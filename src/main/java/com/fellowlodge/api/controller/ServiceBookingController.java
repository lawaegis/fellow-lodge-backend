package com.fellowlodge.api.controller;

import com.fellowlodge.api.common.ApiResponse;
import com.fellowlodge.api.common.PageResponse;
import com.fellowlodge.api.entity.ServiceBooking;
import com.fellowlodge.api.service.ServiceBookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/service-bookings")
@RequiredArgsConstructor
public class ServiceBookingController {

    private final ServiceBookingService serviceBookingService;

    @GetMapping
    @PreAuthorize("hasAuthority('BOOKINGS:READ')")
    public ApiResponse<List<ServiceBooking>> findAll(@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "20") int size,
                                                     @RequestParam(required = false) String sort,
                                                     @RequestParam(required = false) String status,
                                                     @RequestParam(required = false) UUID guestId) {
        Page<ServiceBooking> result = serviceBookingService.findAll(page, size, sort, status, guestId);
        return ApiResponse.ok(result.getContent(), PageResponse.from(result));
    }

    @GetMapping("/guest/{guestId}")
    @PreAuthorize("hasAuthority('BOOKINGS:READ')")
    public ApiResponse<List<ServiceBooking>> byGuest(@PathVariable UUID guestId) {
        return ApiResponse.ok(serviceBookingService.findByGuestId(guestId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('BOOKINGS:READ')")
    public ApiResponse<ServiceBooking> findById(@PathVariable UUID id) {
        return ApiResponse.ok(serviceBookingService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('BOOKINGS:WRITE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ServiceBooking> create(@Valid @RequestBody ServiceBooking booking) {
        return ApiResponse.created("Service booking created", serviceBookingService.create(booking));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('BOOKINGS:WRITE')")
    public ApiResponse<ServiceBooking> update(@PathVariable UUID id, @Valid @RequestBody ServiceBooking booking) {
        return ApiResponse.ok("Service booking updated", serviceBookingService.update(id, booking));
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasAuthority('BOOKINGS:WRITE')")
    public ApiResponse<ServiceBooking> confirm(@PathVariable UUID id) {
        return ApiResponse.ok("Service booking confirmed", serviceBookingService.confirm(id));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('BOOKINGS:WRITE')")
    public ApiResponse<ServiceBooking> complete(@PathVariable UUID id) {
        return ApiResponse.ok("Service booking completed", serviceBookingService.complete(id));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('BOOKINGS:WRITE')")
    public ApiResponse<ServiceBooking> cancel(@PathVariable UUID id, @RequestParam(required = false) String reason) {
        return ApiResponse.ok("Service booking cancelled", serviceBookingService.cancel(id, reason));
    }
}
