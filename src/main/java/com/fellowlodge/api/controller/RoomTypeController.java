package com.fellowlodge.api.controller;

import com.fellowlodge.api.common.ApiResponse;
import com.fellowlodge.api.common.PageResponse;
import com.fellowlodge.api.entity.RoomType;
import com.fellowlodge.api.service.RoomTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
@RequestMapping("/api/room-types")
@RequiredArgsConstructor
public class RoomTypeController {

    private final RoomTypeService roomTypeService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROOM_TYPES:READ')")
    public ApiResponse<List<RoomType>> findAll(@RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "20") int size,
                                               @RequestParam(required = false) String sort,
                                               @RequestParam(required = false) Boolean active) {
        Page<RoomType> result = roomTypeService.findAll(page, size, sort, active);
        return ApiResponse.ok(result.getContent(), PageResponse.from(result));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ROOM_TYPES:READ')")
    public ApiResponse<List<RoomType>> findAll() {
        return ApiResponse.ok(roomTypeService.findAll());
    }

    @GetMapping("/active")
    @PreAuthorize("hasAuthority('ROOM_TYPES:READ')")
    public ApiResponse<List<RoomType>> active() {
        return ApiResponse.ok(roomTypeService.findActive());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROOM_TYPES:READ')")
    public ApiResponse<RoomType> findById(@PathVariable UUID id) {
        return ApiResponse.ok(roomTypeService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROOM_TYPES:WRITE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RoomType> create(@Valid @RequestBody RoomType roomType) {
        return ApiResponse.created("Room type created", roomTypeService.create(roomType));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROOM_TYPES:WRITE')")
    public ApiResponse<RoomType> update(@PathVariable UUID id, @Valid @RequestBody RoomType roomType) {
        return ApiResponse.ok("Room type updated", roomTypeService.update(id, roomType));
    }

    @PostMapping("/{id}/amenities/{amenityId}")
    @PreAuthorize("hasAuthority('ROOM_TYPES:WRITE')")
    public ApiResponse<RoomType> addAmenity(@PathVariable UUID id, @PathVariable UUID amenityId) {
        return ApiResponse.ok("Amenity added", roomTypeService.addAmenity(id, amenityId));
    }

    @DeleteMapping("/{id}/amenities/{amenityId}")
    @PreAuthorize("hasAuthority('ROOM_TYPES:WRITE')")
    public ApiResponse<RoomType> removeAmenity(@PathVariable UUID id, @PathVariable UUID amenityId) {
        return ApiResponse.ok("Amenity removed", roomTypeService.removeAmenity(id, amenityId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROOM_TYPES:DELETE')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        roomTypeService.delete(id);
        return ApiResponse.deleted("Room type deleted");
    }
}
