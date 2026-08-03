package com.fellowlodge.api.controller;

import com.fellowlodge.api.common.ApiResponse;
import com.fellowlodge.api.entity.CheckIn;
import com.fellowlodge.api.service.CheckInService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/check-ins")
@RequiredArgsConstructor
public class CheckInController {

    private final CheckInService checkInService;

    @GetMapping
    @PreAuthorize("hasAuthority('RESERVATIONS:READ')")
    public ApiResponse<List<CheckIn>> findByRoom(@RequestParam(required = false) UUID roomId,
                                                 @RequestParam(required = false) UUID reservationId) {
        if (reservationId != null) {
            CheckIn checkIn = checkInService.findByReservationId(reservationId);
            return ApiResponse.ok(checkIn == null ? List.of() : List.of(checkIn));
        }
        if (roomId != null) {
            return ApiResponse.ok(checkInService.findByRoomId(roomId));
        }
        return ApiResponse.ok(checkInService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('RESERVATIONS:READ')")
    public ApiResponse<CheckIn> findById(@PathVariable UUID id) {
        return ApiResponse.ok(checkInService.findById(id));
    }
}
