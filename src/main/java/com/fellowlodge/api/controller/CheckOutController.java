package com.fellowlodge.api.controller;

import com.fellowlodge.api.common.ApiResponse;
import com.fellowlodge.api.entity.CheckOut;
import com.fellowlodge.api.service.CheckOutService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/check-outs")
@RequiredArgsConstructor
public class CheckOutController {

    private final CheckOutService checkOutService;

    @GetMapping
    @PreAuthorize("hasAuthority('RESERVATIONS:READ')")
    public ApiResponse<List<CheckOut>> findAll() {
        return ApiResponse.ok(checkOutService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('RESERVATIONS:READ')")
    public ApiResponse<CheckOut> findById(@PathVariable UUID id) {
        return ApiResponse.ok(checkOutService.findById(id));
    }

    @GetMapping("/by-check-in/{checkInId}")
    @PreAuthorize("hasAuthority('RESERVATIONS:READ')")
    public ApiResponse<CheckOut> findByCheckIn(@PathVariable UUID checkInId) {
        return ApiResponse.ok(checkOutService.findByCheckInId(checkInId));
    }
}
