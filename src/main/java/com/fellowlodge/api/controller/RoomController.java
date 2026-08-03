package com.fellowlodge.api.controller;

import com.fellowlodge.api.common.ApiResponse;
import com.fellowlodge.api.common.PageResponse;
import com.fellowlodge.api.entity.Room;
import com.fellowlodge.api.enums.RoomStatus;
import com.fellowlodge.api.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROOMS:READ')")
    public ApiResponse<List<Room>> findAll(@RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "20") int size,
                                           @RequestParam(required = false) String sort,
                                           @RequestParam(required = false) String search,
                                           @RequestParam(required = false) String status,
                                           @RequestParam(required = false) UUID roomTypeId,
                                           @RequestParam(required = false) Integer floor) {
        Page<Room> result = roomService.findAll(page, size, sort, search, status, roomTypeId, floor);
        return ApiResponse.ok(result.getContent(), PageResponse.from(result));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ROOMS:READ')")
    public ApiResponse<List<Room>> findAll() {
        return ApiResponse.ok(roomService.findAll());
    }

    @GetMapping("/available")
    @PreAuthorize("hasAuthority('ROOMS:READ')")
    public ApiResponse<List<Room>> available(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            return ApiResponse.ok(roomService.findAvailable());
        }
        return ApiResponse.ok(roomService.findAvailableForDates(checkIn, checkOut));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROOMS:READ')")
    public ApiResponse<Room> findById(@PathVariable UUID id) {
        return ApiResponse.ok(roomService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROOMS:WRITE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Room> create(@Valid @RequestBody Room room) {
        return ApiResponse.created("Room created", roomService.create(room));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROOMS:WRITE')")
    public ApiResponse<Room> update(@PathVariable UUID id, @Valid @RequestBody Room room) {
        return ApiResponse.ok("Room updated", roomService.update(id, room));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ROOMS:WRITE')")
    public ApiResponse<Room> changeStatus(@PathVariable UUID id, @RequestParam RoomStatus status) {
        return ApiResponse.ok("Room status changed", roomService.changeStatus(id, status));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROOMS:DELETE')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        roomService.delete(id);
        return ApiResponse.deleted("Room deleted");
    }
}
