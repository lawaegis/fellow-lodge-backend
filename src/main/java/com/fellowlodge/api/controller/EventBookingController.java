package com.fellowlodge.api.controller;

import com.fellowlodge.api.common.ApiResponse;
import com.fellowlodge.api.common.PageResponse;
import com.fellowlodge.api.entity.EventBooking;
import com.fellowlodge.api.security.SecurityUtils;
import com.fellowlodge.api.service.EventBookingService;
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
@RequestMapping("/api/event-bookings")
@RequiredArgsConstructor
public class EventBookingController {

    private final EventBookingService eventBookingService;

    @GetMapping
    @PreAuthorize("hasAuthority('BOOKINGS:READ')")
    public ApiResponse<List<EventBooking>> findAll(@RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size,
                                                   @RequestParam(required = false) String sort,
                                                   @RequestParam(required = false) String status,
                                                   @RequestParam(required = false) UUID guestId,
                                                   @RequestParam(required = false) UUID eventId) {
        Page<EventBooking> result = eventBookingService.findAll(page, size, sort, status, guestId, eventId);
        return ApiResponse.ok(result.getContent(), PageResponse.from(result));
    }

    @GetMapping("/guest/{guestId}")
    @PreAuthorize("hasAuthority('BOOKINGS:READ')")
    public ApiResponse<List<EventBooking>> byGuest(@PathVariable UUID guestId) {
        return ApiResponse.ok(eventBookingService.findByGuestId(guestId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('BOOKINGS:READ')")
    public ApiResponse<EventBooking> findById(@PathVariable UUID id) {
        return ApiResponse.ok(eventBookingService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('BOOKINGS:WRITE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<EventBooking> create(@Valid @RequestBody EventBooking booking) {
        return ApiResponse.created("Event booking created",
                eventBookingService.create(booking, SecurityUtils.currentUserId()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('BOOKINGS:WRITE')")
    public ApiResponse<EventBooking> update(@PathVariable UUID id, @Valid @RequestBody EventBooking booking) {
        return ApiResponse.ok("Event booking updated", eventBookingService.update(id, booking));
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasAuthority('BOOKINGS:WRITE')")
    public ApiResponse<EventBooking> confirm(@PathVariable UUID id) {
        return ApiResponse.ok("Event booking confirmed", eventBookingService.confirm(id));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('BOOKINGS:WRITE')")
    public ApiResponse<EventBooking> complete(@PathVariable UUID id) {
        return ApiResponse.ok("Event booking completed", eventBookingService.complete(id));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('BOOKINGS:WRITE')")
    public ApiResponse<EventBooking> cancel(@PathVariable UUID id, @RequestParam(required = false) String reason) {
        return ApiResponse.ok("Event booking cancelled", eventBookingService.cancel(id, reason));
    }
}
