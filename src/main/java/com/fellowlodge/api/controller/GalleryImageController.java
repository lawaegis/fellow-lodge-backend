package com.fellowlodge.api.controller;

import com.fellowlodge.api.common.ApiResponse;
import com.fellowlodge.api.entity.GalleryImage;
import com.fellowlodge.api.service.GalleryImageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/gallery")
@RequiredArgsConstructor
public class GalleryImageController {

    private final GalleryImageService galleryImageService;

    @GetMapping
    @PreAuthorize("hasAuthority('GALLERY:WRITE') or hasAuthority('REVIEWS:READ')")
    public ApiResponse<List<GalleryImage>> findAll() {
        return ApiResponse.ok(galleryImageService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('GALLERY:WRITE')")
    public ApiResponse<GalleryImage> findById(@PathVariable UUID id) {
        return ApiResponse.ok(galleryImageService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('GALLERY:WRITE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<GalleryImage> create(@Valid @RequestBody GalleryImage image) {
        return ApiResponse.created("Gallery image added", galleryImageService.create(image));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('GALLERY:WRITE')")
    public ApiResponse<GalleryImage> update(@PathVariable UUID id, @Valid @RequestBody GalleryImage image) {
        return ApiResponse.ok("Gallery image updated", galleryImageService.update(id, image));
    }

    @PostMapping("/{id}/active")
    @PreAuthorize("hasAuthority('GALLERY:WRITE')")
    public ApiResponse<GalleryImage> setActive(@PathVariable UUID id, @RequestParam boolean active) {
        return ApiResponse.ok("Gallery image updated", galleryImageService.setActive(id, active));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('GALLERY:WRITE')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        galleryImageService.delete(id);
        return ApiResponse.deleted("Gallery image deleted");
    }
}
