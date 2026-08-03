package com.fellowlodge.api.controller;

import com.fellowlodge.api.common.ApiResponse;
import com.fellowlodge.api.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileStorageService fileStorageService;

    @GetMapping
    @PreAuthorize("hasAuthority('FILES:WRITE')")
    public ApiResponse<List<FileStorageService.StoredFile>> listFiles() {
        return ApiResponse.ok(fileStorageService.listFiles());
    }

    @PostMapping("/room-image")
    @PreAuthorize("hasAuthority('FILES:WRITE')")
    public ApiResponse<Map<String, String>> uploadRoomImage(@RequestParam("file") MultipartFile file) {
        return ApiResponse.ok("Room image uploaded",
                Map.of("path", fileStorageService.storeRoomImage(file)));
    }

    @PostMapping("/gallery")
    @PreAuthorize("hasAuthority('GALLERY:WRITE')")
    public ApiResponse<Map<String, String>> uploadGalleryImage(@RequestParam("file") MultipartFile file) {
        return ApiResponse.ok("Gallery image uploaded",
                Map.of("path", fileStorageService.storeGalleryImage(file)));
    }

    @PostMapping("/event-image")
    @PreAuthorize("hasAuthority('FILES:WRITE')")
    public ApiResponse<Map<String, String>> uploadEventImage(@RequestParam("file") MultipartFile file) {
        return ApiResponse.ok("Event image uploaded",
                Map.of("path", fileStorageService.storeEventImage(file)));
    }

    @PostMapping("/conference-image")
    @PreAuthorize("hasAuthority('FILES:WRITE')")
    public ApiResponse<Map<String, String>> uploadConferenceImage(@RequestParam("file") MultipartFile file) {
        return ApiResponse.ok("Conference image uploaded",
                Map.of("path", fileStorageService.storeConferenceImage(file)));
    }

    @PostMapping("/banner")
    @PreAuthorize("hasAuthority('FILES:WRITE')")
    public ApiResponse<Map<String, String>> uploadBannerImage(@RequestParam("file") MultipartFile file) {
        return ApiResponse.ok("Banner image uploaded",
                Map.of("path", fileStorageService.storeBannerImage(file)));
    }

    @PostMapping("/menu-item")
    @PreAuthorize("hasAuthority('FILES:WRITE')")
    public ApiResponse<Map<String, String>> uploadMenuItemImage(@RequestParam("file") MultipartFile file) {
        return ApiResponse.ok("Menu item image uploaded",
                Map.of("path", fileStorageService.storeMenuItemImage(file)));
    }

    @PostMapping("/menu-category")
    @PreAuthorize("hasAuthority('FILES:WRITE')")
    public ApiResponse<Map<String, String>> uploadMenuCategoryImage(@RequestParam("file") MultipartFile file) {
        return ApiResponse.ok("Menu category image uploaded",
                Map.of("path", fileStorageService.store(file, "menu")));
    }

    @PostMapping("/attraction")
    @PreAuthorize("hasAuthority('FILES:WRITE')")
    public ApiResponse<Map<String, String>> uploadAttractionImage(@RequestParam("file") MultipartFile file) {
        return ApiResponse.ok("Attraction image uploaded",
                Map.of("path", fileStorageService.store(file, "attractions")));
    }

    @PostMapping("/hotel-service")
    @PreAuthorize("hasAuthority('FILES:WRITE')")
    public ApiResponse<Map<String, String>> uploadHotelServiceImage(@RequestParam("file") MultipartFile file) {
        return ApiResponse.ok("Hotel service image uploaded",
                Map.of("path", fileStorageService.store(file, "services")));
    }

    @PostMapping("/conference-hall")
    @PreAuthorize("hasAuthority('FILES:WRITE')")
    public ApiResponse<Map<String, String>> uploadConferenceHallImage(@RequestParam("file") MultipartFile file) {
        return ApiResponse.ok("Conference hall image uploaded",
                Map.of("path", fileStorageService.store(file, "conferences")));
    }

    @PostMapping("/profile")
    @PreAuthorize("hasAuthority('PROFILE:WRITE')")
    public ApiResponse<Map<String, String>> uploadProfileImage(@RequestParam("file") MultipartFile file) {
        return ApiResponse.ok("Profile image uploaded",
                Map.of("path", fileStorageService.storeProfileImage(file)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('FILES:WRITE')")
    public ApiResponse<Map<String, String>> upload(@RequestParam("file") MultipartFile file,
                                                   @RequestParam(defaultValue = "misc") String folder,
                                                   @RequestParam(required = false) String filename) {
        return ApiResponse.ok("File uploaded",
                Map.of("path", fileStorageService.store(file, folder, filename)));
    }
}
