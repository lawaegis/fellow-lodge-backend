package com.fellowlodge.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fellowlodge.api.common.exception.BusinessException;
import com.fellowlodge.api.config.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Stores uploaded files (room images, gallery images, guest photos, ...) and
 * returns a displayable path/URL that is persisted on the entity.
 *
 * <p>Two backends are supported:
 * <ul>
 *   <li><b>Supabase Storage</b> (production) - used whenever a
 *       {@code SUPABASE_SERVICE_ROLE_KEY} is configured. Uploads go
 *       server-side to a public storage bucket via the service-role key, which
 *       is never exposed to clients. A public object URL is returned and stored
 *       in PostgreSQL.</li>
 *   <li><b>Local filesystem</b> (development / tests) - files land under
 *       {@code app.storage.upload-dir} and are served from {@code /uploads/**}.
 *       The returned value keeps the legacy {@code /uploads/...} shape.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private static final String[] ALLOWED_EXTENSIONS = {"jpg", "jpeg", "png", "gif", "webp", "pdf"};
    private static final Duration STORAGE_TIMEOUT = Duration.ofSeconds(60);
    private static final int LIST_LIMIT = 1000;

    private final AppProperties appProperties;
    private final ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    /** Buckets we have already tried to create this run. */
    private final Set<String> ensuredBuckets = ConcurrentHashMap.newKeySet();

    // ============ PUBLIC API ============

    public String store(MultipartFile file, String folder) {
        return store(file, folder, null);
    }

    public String store(MultipartFile file, String folder, String filename) {
        validate(file);
        if (useSupabaseStorage()) {
            return uploadToSupabase(file, folder, filename);
        }
        return storeLocal(file, folder, filename);
    }

    public String storeProfileImage(MultipartFile file) {
        return store(file, "profiles");
    }

    public String storeRoomImage(MultipartFile file) {
        return store(file, "rooms");
    }

    public String storeGalleryImage(MultipartFile file) {
        return store(file, "gallery");
    }

    public String storeEventImage(MultipartFile file) {
        return store(file, "events");
    }

    public String storeConferenceImage(MultipartFile file) {
        return store(file, "conferences");
    }

    public String storeBannerImage(MultipartFile file) {
        return store(file, "banners");
    }

    public String storeMenuItemImage(MultipartFile file) {
        return store(file, "menu");
    }

    public record StoredFile(String relativePath, long size) {
    }

    public List<StoredFile> listFiles() {
        if (useSupabaseStorage()) {
            return listSupabaseFiles();
        }
        return listLocalFiles();
    }

    public byte[] load(String relativePath) {
        if (useSupabaseStorage()) {
            return loadFromSupabase(relativePath);
        }
        return loadLocal(relativePath);
    }

    public void delete(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return;
        }
        if (useSupabaseStorage()) {
            deleteFromSupabase(relativePath);
            return;
        }
        try {
            Path resolved = resolveAgainstRoot(relativePath);
            Files.deleteIfExists(resolved);
        } catch (IOException e) {
            log.warn("Failed to delete file {}", relativePath, e);
        }
    }

    // ============ SUPABASE STORAGE ============

    private boolean useSupabaseStorage() {
        return StringUtils.hasText(appProperties.getStorage().getServiceRoleKey());
    }

    private String storageBase() {
        String base = SupabaseStoragePaths.storageBaseUrl(appProperties.getStorage().getSupabaseUrl());
        if (base == null) {
            throw new BusinessException(
                    "Supabase Storage is not configured: set SUPABASE_URL together with SUPABASE_SERVICE_ROLE_KEY.",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return base;
    }

    private String uploadToSupabase(MultipartFile file, String folder, String filename) {
        String bucket = SupabaseStoragePaths.sanitizeBucket(folder);
        String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "file" : file.getOriginalFilename());
        String name = resolveObjectKey(original, filename);
        ensureBucket(bucket);

        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType)) {
            contentType = guessContentType(getExtension(original));
        }
        String base = storageBase();
        String url = base + "/object/" + bucket + "/" + name;
        try {
            HttpResponse<String> response = httpClient.send(
                    HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .timeout(STORAGE_TIMEOUT)
                            .header("Authorization", "Bearer " + appProperties.getStorage().getServiceRoleKey())
                            .header("apikey", appProperties.getStorage().getServiceRoleKey())
                            .header("Content-Type", contentType)
                            .PUT(HttpRequest.BodyPublishers.ofByteArray(file.getBytes()))
                            .build(),
                    HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new BusinessException("Storage upload failed (HTTP " + response.statusCode() + "): "
                        + truncate(response.body()), HttpStatus.BAD_GATEWAY);
            }
            log.info("Uploaded object to Supabase Storage: {}/{}", bucket, name);
            return SupabaseStoragePaths.publicObjectUrl(base, bucket, name);
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("Could not upload to Supabase Storage: " + e.getMessage(),
                    HttpStatus.BAD_GATEWAY);
        }
    }

    private void ensureBucket(String bucket) {
        if (ensuredBuckets.contains(bucket)) {
            return;
        }
        String base = storageBase();
        String payload = "{\"id\":\"" + bucket + "\",\"name\":\"" + bucket + "\",\"public\":true}";
        try {
            HttpResponse<String> response = httpClient.send(
                    HttpRequest.newBuilder()
                            .uri(URI.create(base + "/bucket"))
                            .timeout(STORAGE_TIMEOUT)
                            .header("Authorization", "Bearer " + appProperties.getStorage().getServiceRoleKey())
                            .header("apikey", appProperties.getStorage().getServiceRoleKey())
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(payload))
                            .build(),
                    HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            if (status >= 200 && status < 300) {
                ensuredBuckets.add(bucket);
                log.info("Created Supabase Storage bucket: {}", bucket);
            } else {
                String body = response.body();
                boolean duplicate = status == 409 || status == 400
                        || body.toLowerCase().contains("duplicate")
                        || body.toLowerCase().contains("already exists")
                        || body.toLowerCase().contains("exists");
                if (duplicate) {
                    ensuredBuckets.add(bucket);
                    log.debug("Supabase Storage bucket already exists: {}", bucket);
                } else {
                    log.warn("Could not create Supabase Storage bucket {} (HTTP {}): {}", bucket, status, truncate(body));
                }
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Could not create Supabase Storage bucket {}: {}", bucket, e.getMessage());
        }
    }

    private List<StoredFile> listSupabaseFiles() {
        List<StoredFile> result = new ArrayList<>();
        String base = storageBase();
        for (String bucket : configuredBuckets()) {
            try {
                HttpResponse<String> response = httpClient.send(
                        HttpRequest.newBuilder()
                                .uri(URI.create(base + "/object/list/" + bucket))
                                .timeout(STORAGE_TIMEOUT)
                                .header("Authorization", "Bearer " + appProperties.getStorage().getServiceRoleKey())
                                .header("apikey", appProperties.getStorage().getServiceRoleKey())
                                .header("Content-Type", "application/json")
                                .POST(HttpRequest.BodyPublishers.ofString(
                                        "{\"prefix\":\"\",\"limit\":" + LIST_LIMIT + ",\"offset\":0}"))
                                .build(),
                        HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    log.warn("Could not list bucket {} (HTTP {}): {}", bucket, response.statusCode(), truncate(response.body()));
                    continue;
                }
                JsonNode array = objectMapper.readTree(response.body());
                if (array != null && array.isArray()) {
                    for (JsonNode node : array) {
                        String name = node.path("name").asText(null);
                        long size = node.path("metadata").path("size").asLong(0);
                        if (name != null && !name.isBlank()) {
                            result.add(new StoredFile(bucket + "/" + name, size));
                        }
                    }
                }
            } catch (IOException | InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Could not list bucket {}: {}", bucket, e.getMessage());
            }
        }
        return result;
    }

    private void deleteFromSupabase(String pathOrUrl) {
        String[] bucketAndKey = SupabaseStoragePaths.parseBucketAndKey(pathOrUrl);
        if (bucketAndKey == null) {
            log.warn("Cannot delete: could not determine bucket/key from {}", pathOrUrl);
            return;
        }
        String base = storageBase();
        String url = base + "/object/" + bucketAndKey[0] + "/" + bucketAndKey[1];
        try {
            HttpResponse<String> response = httpClient.send(
                    HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .timeout(STORAGE_TIMEOUT)
                            .header("Authorization", "Bearer " + appProperties.getStorage().getServiceRoleKey())
                            .header("apikey", appProperties.getStorage().getServiceRoleKey())
                            .DELETE()
                            .build(),
                    HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.warn("Could not delete {} (HTTP {}): {}", pathOrUrl, response.statusCode(), truncate(response.body()));
            } else {
                log.info("Deleted Supabase Storage object: {}", pathOrUrl);
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Could not delete {}: {}", pathOrUrl, e.getMessage());
        }
    }

    private byte[] loadFromSupabase(String pathOrUrl) {
        String[] bucketAndKey = SupabaseStoragePaths.parseBucketAndKey(pathOrUrl);
        if (bucketAndKey == null) {
            throw new BusinessException("Invalid file reference: " + pathOrUrl, HttpStatus.NOT_FOUND);
        }
        String url = storageBase() + "/object/" + bucketAndKey[0] + "/" + bucketAndKey[1];
        try {
            HttpResponse<byte[]> response = httpClient.send(
                    HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .timeout(STORAGE_TIMEOUT)
                            .header("Authorization", "Bearer " + appProperties.getStorage().getServiceRoleKey())
                            .header("apikey", appProperties.getStorage().getServiceRoleKey())
                            .GET()
                            .build(),
                    HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new BusinessException("File not found in Supabase Storage (HTTP " + response.statusCode() + ")",
                        HttpStatus.NOT_FOUND);
            }
            return response.body();
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("Could not read file from Supabase Storage: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /** Eagerly provisions the configured buckets on startup (idempotent, non-fatal). */
    @EventListener(ApplicationReadyEvent.class)
    public void ensureConfiguredBuckets() {
        if (!useSupabaseStorage()) {
            return;
        }
        for (String bucket : configuredBuckets()) {
            ensureBucket(bucket);
        }
    }

    private List<String> configuredBuckets() {
        List<String> buckets = new ArrayList<>();
        String configured = appProperties.getStorage().getBuckets();
        if (StringUtils.hasText(configured)) {
            for (String part : configured.split(",")) {
                String bucket = SupabaseStoragePaths.sanitizeBucket(part);
                if (!buckets.contains(bucket)) {
                    buckets.add(bucket);
                }
            }
        }
        if (buckets.isEmpty()) {
            buckets.addAll(List.of("rooms", "gallery", "events", "conferences", "banners", "menu", "profiles"));
        }
        return buckets;
    }

    // ============ LOCAL FILESYSTEM ============

    private String storeLocal(MultipartFile file, String folder, String filename) {
        String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "file" : file.getOriginalFilename());
        String extension = getExtension(original);
        if (!isAllowed(extension)) {
            throw new BusinessException("File type not allowed: ." + extension, HttpStatus.BAD_REQUEST);
        }
        try {
            Path root = uploadRoot();
            Path dir = root.resolve(sanitize(folder));
            Files.createDirectories(dir);

            String name = sanitizeName(filename);
            if (name == null || name.isBlank()) {
                name = UUID.randomUUID() + (extension.isEmpty() ? "" : "." + extension);
            } else {
                String nameExt = getExtension(name);
                if (nameExt.isEmpty() && !extension.isEmpty()) {
                    name = name + "." + extension;
                }
                if (!isAllowed(getExtension(name))) {
                    throw new BusinessException("File type not allowed: ." + getExtension(name), HttpStatus.BAD_REQUEST);
                }
            }

            Path target = dir.resolve(name).normalize();
            if (!target.startsWith(root)) {
                throw new BusinessException("Invalid file path.", HttpStatus.BAD_REQUEST);
            }
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return SupabaseStoragePaths.LEGACY_UPLOADS_PREFIX + folder + "/" + name;
        } catch (IOException e) {
            log.error("Failed to store file", e);
            throw new BusinessException("Failed to store file: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private List<StoredFile> listLocalFiles() {
        Path root = uploadRoot();
        if (!Files.isDirectory(root)) {
            return List.of();
        }
        try (Stream<Path> walk = Files.walk(root)) {
            return walk.filter(Files::isRegularFile)
                    .map(p -> new StoredFile(root.relativize(p).toString().replace('\\', '/'), p.toFile().length()))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.warn("Failed to list uploaded files", e);
            return new ArrayList<>();
        }
    }

    private byte[] loadLocal(String relativePath) {
        try {
            Path resolved = resolveAgainstRoot(relativePath);
            if (!Files.exists(resolved) || !Files.isRegularFile(resolved)) {
                throw new BusinessException("File not found: " + relativePath, HttpStatus.NOT_FOUND);
            }
            return Files.readAllBytes(resolved);
        } catch (IOException e) {
            throw new BusinessException("Failed to read file: " + relativePath, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ============ SHARED HELPERS ============

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("No file was uploaded.", HttpStatus.BAD_REQUEST);
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("File exceeds the maximum allowed size of 10MB.", HttpStatus.BAD_REQUEST);
        }
        String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "file" : file.getOriginalFilename());
        if (!isAllowed(getExtension(original))) {
            throw new BusinessException("File type not allowed: ." + getExtension(original), HttpStatus.BAD_REQUEST);
        }
    }

    /** Builds the object key (file name) for Supabase Storage, validating the extension. */
    private String resolveObjectKey(String originalFilename, String requestedName) {
        String originalExtension = getExtension(originalFilename);
        String name = sanitizeName(requestedName);
        if (name == null || name.isBlank()) {
            return UUID.randomUUID() + (originalExtension.isEmpty() ? "" : "." + originalExtension);
        }
        String nameExtension = getExtension(name);
        if (nameExtension.isEmpty() && !originalExtension.isEmpty()) {
            name = name + "." + originalExtension;
        }
        if (!isAllowed(getExtension(name))) {
            throw new BusinessException("File type not allowed: ." + getExtension(name), HttpStatus.BAD_REQUEST);
        }
        return name;
    }

    private String guessContentType(String extension) {
        return switch (extension.toLowerCase(Locale.ROOT)) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            case "pdf" -> "application/pdf";
            default -> "application/octet-stream";
        };
    }

    private Path resolveAgainstRoot(String relativePath) throws IOException {
        Path root = uploadRoot().normalize();
        Path resolved = root.resolve(sanitize(relativePath)).normalize();
        if (!resolved.startsWith(root)) {
            throw new BusinessException("Invalid file path.", HttpStatus.BAD_REQUEST);
        }
        return resolved;
    }

    private Path uploadRoot() {
        Path path = Paths.get(appProperties.getStorage().getUploadDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new BusinessException("Could not initialize storage directory.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return path;
    }

    private String sanitize(String path) {
        return path == null ? "misc" : path.replaceAll("[\\.]{2,}", "_").replace("\\", "/");
    }

    private String sanitizeName(String name) {
        if (name == null) return null;
        String cleaned = name.replaceAll("[^a-zA-Z0-9._-]", "_").replace("\\", "/");
        cleaned = cleaned.substring(cleaned.lastIndexOf('/') + 1);
        cleaned = cleaned.replaceAll("[\\.]{2,}", "_");
        if (!cleaned.isEmpty() && cleaned.charAt(0) == '.') {
            cleaned = cleaned.substring(1);
        }
        return cleaned;
    }

    private String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot + 1).toLowerCase(Locale.ROOT) : "";
    }

    private boolean isAllowed(String extension) {
        for (String allowed : ALLOWED_EXTENSIONS) {
            if (allowed.equals(extension)) {
                return true;
            }
        }
        return false;
    }

    private String truncate(String value) {
        if (value == null) return "";
        return value.length() <= 200 ? value : value.substring(0, 200) + "...";
    }
}
