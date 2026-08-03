package com.fellowlodge.api.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SupabaseStoragePathsTest {

    @Test
    @DisplayName("storageBaseUrl builds the v1 API base from a project URL")
    void storageBaseUrl() {
        assertThat(SupabaseStoragePaths.storageBaseUrl("https://abc123.supabase.co"))
                .isEqualTo("https://abc123.supabase.co/storage/v1");
        assertThat(SupabaseStoragePaths.storageBaseUrl("  https://abc123.supabase.co/  "))
                .isEqualTo("https://abc123.supabase.co/storage/v1");
        assertThat(SupabaseStoragePaths.storageBaseUrl(" ")).isNull();
        assertThat(SupabaseStoragePaths.storageBaseUrl(null)).isNull();
    }

    @Test
    @DisplayName("publicObjectUrl points at the public read URL")
    void publicObjectUrl() {
        assertThat(SupabaseStoragePaths.publicObjectUrl("https://abc123.supabase.co/storage/v1", "rooms", "a.jpg"))
                .isEqualTo("https://abc123.supabase.co/storage/v1/object/public/rooms/a.jpg");
    }

    @Test
    @DisplayName("sanitizeBucket produces valid Supabase bucket ids")
    void sanitizeBucket() {
        assertThat(SupabaseStoragePaths.sanitizeBucket("Rooms")).isEqualTo("rooms");
        assertThat(SupabaseStoragePaths.sanitizeBucket("  Hotel Services  ")).isEqualTo("hotel-services");
        assertThat(SupabaseStoragePaths.sanitizeBucket("A!!B??C")).isEqualTo("a-b-c");
        assertThat(SupabaseStoragePaths.sanitizeBucket("--")).isEqualTo("misc");
        assertThat(SupabaseStoragePaths.sanitizeBucket("")).isEqualTo("misc");
        assertThat(SupabaseStoragePaths.sanitizeBucket(null)).isEqualTo("misc");
    }

    @Test
    @DisplayName("parseBucketAndKey accepts public URLs, legacy uploads paths and bare bucket/key")
    void parseBucketAndKey() {
        assertThat(SupabaseStoragePaths.parseBucketAndKey("https://abc.supabase.co/storage/v1/object/public/rooms/a.jpg"))
                .containsExactly("rooms", "a.jpg");
        assertThat(SupabaseStoragePaths.parseBucketAndKey("/uploads/rooms/a.jpg"))
                .containsExactly("rooms", "a.jpg");
        assertThat(SupabaseStoragePaths.parseBucketAndKey("rooms/a.jpg"))
                .containsExactly("rooms", "a.jpg");
        assertThat(SupabaseStoragePaths.parseBucketAndKey("menu/sub/dir/b.jpg"))
                .containsExactly("menu", "sub/dir/b.jpg");
        assertThat(SupabaseStoragePaths.parseBucketAndKey("rooms/")).isNull();
        assertThat(SupabaseStoragePaths.parseBucketAndKey("no-slash-here")).isNull();
        assertThat(SupabaseStoragePaths.parseBucketAndKey("")).isNull();
        assertThat(SupabaseStoragePaths.parseBucketAndKey(null)).isNull();
    }
}
