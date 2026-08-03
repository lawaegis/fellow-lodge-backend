package com.fellowlodge.api.controller;

import com.fellowlodge.api.common.ApiResponse;
import com.fellowlodge.api.entity.Amenity;
import com.fellowlodge.api.service.AmenityService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/amenities")
@RequiredArgsConstructor
public class AmenityController {

    private final AmenityService amenityService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROOM_TYPES:READ')")
    public ApiResponse<List<Amenity>> findAll() {
        return ApiResponse.ok(amenityService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROOM_TYPES:READ')")
    public ApiResponse<Amenity> findById(@PathVariable UUID id) {
        return ApiResponse.ok(amenityService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROOM_TYPES:WRITE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Amenity> create(@Valid @RequestBody Amenity amenity) {
        return ApiResponse.created("Amenity created", amenityService.create(amenity));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROOM_TYPES:WRITE')")
    public ApiResponse<Amenity> update(@PathVariable UUID id, @Valid @RequestBody Amenity amenity) {
        return ApiResponse.ok("Amenity updated", amenityService.update(id, amenity));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROOM_TYPES:DELETE')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        amenityService.delete(id);
        return ApiResponse.deleted("Amenity deleted");
    }
}
