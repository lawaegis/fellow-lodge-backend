package com.fellowlodge.api.controller;

import com.fellowlodge.api.common.ApiResponse;
import com.fellowlodge.api.entity.RoomImage;
import com.fellowlodge.api.service.RoomImageService;
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
@RequestMapping("/api/room-images")
@RequiredArgsConstructor
public class RoomImageController {

    private final RoomImageService roomImageService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROOMS:READ')")
    public ApiResponse<List<RoomImage>> findByRoom(@RequestParam(required = false) UUID roomId,
                                                   @RequestParam(required = false) UUID roomTypeId) {
        if (roomId != null) {
            return ApiResponse.ok(roomImageService.findByRoomId(roomId));
        }
        if (roomTypeId != null) {
            return ApiResponse.ok(roomImageService.findByRoomTypeId(roomTypeId));
        }
        return ApiResponse.ok(List.of());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROOMS:WRITE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RoomImage> add(@Valid @RequestBody RoomImage image) {
        return ApiResponse.created("Room image added", roomImageService.add(image));
    }

    @PutMapping("/{id}/primary")
    @PreAuthorize("hasAuthority('ROOMS:WRITE')")
    public ApiResponse<RoomImage> setPrimary(@PathVariable UUID id) {
        return ApiResponse.ok("Primary image set", roomImageService.setPrimary(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROOMS:WRITE')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        roomImageService.delete(id);
        return ApiResponse.deleted("Room image deleted");
    }
}
