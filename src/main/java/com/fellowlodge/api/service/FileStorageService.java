package com.fellowlodge.api.service;

import com.fellowlodge.api.common.exception.BusinessException;
import com.fellowlodge.api.config.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Stores uploaded files (room images, gallery images, guest photos) on local disk
 * and serves them from the /uploads/** resource handler.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private static final String[] ALLOWED_EXTENSIONS = {"jpg", "jpeg", "png", "gif", "webp", "pdf"};

    private final AppProperties appProperties;

    public String store(MultipartFile file, String folder) {
        return store(file, folder, null);
    }

    public String store(MultipartFile file, String folder, String filename) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("No file was uploaded.", HttpStatus.BAD_REQUEST);
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("File exceeds the maximum allowed size of 10MB.", HttpStatus.BAD_REQUEST);
        }
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
            return folder + "/" + name;
        } catch (IOException e) {
            log.error("Failed to store file", e);
            throw new BusinessException("Failed to store file: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public record StoredFile(String relativePath, long size) {}

    public List<StoredFile> listFiles() {
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

    public byte[] load(String relativePath) {
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

    public void delete(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return;
        }
        try {
            Path resolved = resolveAgainstRoot(relativePath);
            Files.deleteIfExists(resolved);
        } catch (IOException e) {
            log.warn("Failed to delete file {}", relativePath, e);
        }
    }

    public String storeProfileImage(MultipartFile file) {
        String relative = store(file, "profiles");
        return "/uploads/" + relative;
    }

    public String storeRoomImage(MultipartFile file) {
        String relative = store(file, "rooms");
        return "/uploads/" + relative;
    }

    public String storeGalleryImage(MultipartFile file) {
        String relative = store(file, "gallery");
        return "/uploads/" + relative;
    }

    public String storeEventImage(MultipartFile file) {
        String relative = store(file, "events");
        return "/uploads/" + relative;
    }

    public String storeConferenceImage(MultipartFile file) {
        String relative = store(file, "conferences");
        return "/uploads/" + relative;
    }

    public String storeBannerImage(MultipartFile file) {
        String relative = store(file, "banners");
        return "/uploads/" + relative;
    }

    public String storeMenuItemImage(MultipartFile file) {
        String relative = store(file, "menu");
        return "/uploads/" + relative;
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
        return cleaned.replaceAll("[\\.]{2,}", "_");
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
}
