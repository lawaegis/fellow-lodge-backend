package com.fellowlodge.api.service;

import java.util.Locale;

/**
 * Pure helpers for addressing objects in Supabase Storage. Kept free of any
 * IO so the URL/sanitization rules are trivially unit-testable.
 */
public final class SupabaseStoragePaths {

    public static final String OBJECT_PUBLIC_SEGMENT = "/object/public/";
    public static final String LEGACY_UPLOADS_PREFIX = "/uploads/";

    private SupabaseStoragePaths() {
    }

    /**
     * Storage API base, e.g. {@code https://<ref>.supabase.co/storage/v1}.
     * Returns {@code null} for a blank input.
     */
    public static String storageBaseUrl(String supabaseUrl) {
        if (supabaseUrl == null || supabaseUrl.isBlank()) {
            return null;
        }
        String base = supabaseUrl.trim();
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + "/storage/v1";
    }

    /** Publicly readable object URL: {@code <base>/object/public/<bucket>/<key>}. */
    public static String publicObjectUrl(String storageBaseUrl, String bucket, String key) {
        return storageBaseUrl + OBJECT_PUBLIC_SEGMENT + bucket + "/" + key;
    }

    /**
     * Turns an arbitrary folder label into a valid Supabase bucket id
     * (lowercase letters, digits, dashes and underscores).
     */
    public static String sanitizeBucket(String folder) {
        String bucket = folder == null ? "" : folder.trim().toLowerCase(Locale.ROOT);
        bucket = bucket.replaceAll("[^a-z0-9_\\-]", "-").replaceAll("-{2,}", "-");
        bucket = bucket.replaceAll("^-|-$", "");
        return bucket.isEmpty() ? "misc" : bucket;
    }

    /**
     * Splits a stored image value into {@code {bucket, objectKey}}. Accepts a
     * full public URL, a legacy {@code /uploads/<bucket>/<key>} path, or a bare
     * {@code <bucket>/<key>} string. Returns {@code null} when no bucket/key
     * can be extracted.
     */
    public static String[] parseBucketAndKey(String pathOrUrl) {
        if (pathOrUrl == null) {
            return null;
        }
        String value = pathOrUrl.trim();
        int marker = value.indexOf(OBJECT_PUBLIC_SEGMENT);
        if (marker >= 0) {
            value = value.substring(marker + OBJECT_PUBLIC_SEGMENT.length());
        } else if (value.startsWith(LEGACY_UPLOADS_PREFIX)) {
            value = value.substring(LEGACY_UPLOADS_PREFIX.length());
        } else if (value.startsWith("/")) {
            value = value.substring(1);
        }
        if (value.isEmpty()) {
            return null;
        }
        int slash = value.indexOf('/');
        if (slash <= 0 || slash == value.length() - 1) {
            return null;
        }
        return new String[]{value.substring(0, slash), value.substring(slash + 1)};
    }
}
