package com.fellowlodge.api.controller;

import com.fellowlodge.api.common.ApiResponse;
import com.fellowlodge.api.common.PageResponse;
import com.fellowlodge.api.common.exception.ResourceNotFoundException;
import com.fellowlodge.api.dto.booking.BookingRequest;
import com.fellowlodge.api.dto.booking.CancelRequest;
import com.fellowlodge.api.dto.checkin.CheckInRequest;
import com.fellowlodge.api.dto.checkin.CheckOutRequest;
import com.fellowlodge.api.entity.CheckIn;
import com.fellowlodge.api.entity.CheckOut;
import com.fellowlodge.api.entity.Guest;
import com.fellowlodge.api.entity.Reservation;
import com.fellowlodge.api.security.SecurityUtils;
import com.fellowlodge.api.service.GuestService;
import com.fellowlodge.api.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final GuestService guestService;

    @GetMapping
    @PreAuthorize("hasAuthority('RESERVATIONS:READ')")
    public ApiResponse<List<Reservation>> findAll(@RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "20") int size,
                                                  @RequestParam(required = false) String sort,
                                                  @RequestParam(required = false) String search,
                                                  @RequestParam(required = false) String status,
                                                  @RequestParam(required = false) UUID guestId,
                                                  @RequestParam(required = false) UUID roomId,
                                                  @RequestParam(required = false)
                                                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                  @RequestParam(required = false)
                                                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        UUID effectiveGuestId = isGuest() ? currentGuestId() : guestId;
        Page<Reservation> result = reservationService.findAll(page, size, sort, search, status,
                effectiveGuestId, roomId, from, to);
        return ApiResponse.ok(result.getContent(), PageResponse.from(result));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('RESERVATIONS:READ')")
    public ApiResponse<List<Reservation>> findAll() {
        if (isGuest()) {
            return ApiResponse.ok(reservationService.findByGuestId(currentGuestId()));
        }
        return ApiResponse.ok(reservationService.findAll());
    }

    @GetMapping("/today")
    @PreAuthorize("hasAuthority('RESERVATIONS:READ')")
    public ApiResponse<Map<String, List<Reservation>>> today() {
        if (isGuest()) {
            throw new ResourceNotFoundException("Reservations");
        }
        return ApiResponse.ok(Map.of(
                "checkIns", reservationService.findTodayCheckIns(),
                "checkOuts", reservationService.findTodayCheckOuts()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('RESERVATIONS:READ')")
    public ApiResponse<Reservation> findById(@PathVariable UUID id) {
        return ApiResponse.ok(resolveReservation(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('RESERVATIONS:WRITE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Reservation> create(@Valid @RequestBody BookingRequest request) {
        return ApiResponse.created("Reservation created",
                reservationService.createFromRequest(request, SecurityUtils.currentUserId()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('RESERVATIONS:WRITE')")
    public ApiResponse<Reservation> update(@PathVariable UUID id, @Valid @RequestBody Reservation reservation) {
        return ApiResponse.ok("Reservation updated", reservationService.update(id, reservation));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('RESERVATIONS:APPROVE')")
    public ApiResponse<Reservation> approve(@PathVariable UUID id) {
        return ApiResponse.ok("Reservation approved", reservationService.approve(id));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('RESERVATIONS:CANCEL')")
    public ApiResponse<Reservation> cancel(@PathVariable UUID id, @RequestBody(required = false) CancelRequest request) {
        resolveReservation(id);
        return ApiResponse.ok("Reservation cancelled",
                request == null ? reservationService.cancel(id, (String) null) : reservationService.cancel(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('RESERVATIONS:DELETE')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        reservationService.delete(id);
        return ApiResponse.deleted("Reservation deleted");
    }

    @PostMapping("/check-in")
    @PreAuthorize("hasAuthority('CHECK_IN:WRITE')")
    public ApiResponse<CheckIn> checkIn(@Valid @RequestBody CheckInRequest request) {
        return ApiResponse.ok("Guest checked in",
                reservationService.checkIn(request.reservationId(), request, SecurityUtils.currentUserId()));
    }

    @PostMapping("/check-out")
    @PreAuthorize("hasAuthority('CHECK_OUT:WRITE')")
    public ApiResponse<CheckOut> checkOut(@Valid @RequestBody CheckOutRequest request) {
        return ApiResponse.ok("Guest checked out",
                reservationService.checkOut(request.checkInId(), request, SecurityUtils.currentUserId()));
    }

    private boolean isGuest() {
        return "GUEST".equalsIgnoreCase(SecurityUtils.currentRole());
    }

    private UUID currentGuestId() {
        Guest guest = guestService.findByUserId(SecurityUtils.currentUserId());
        return guest == null ? null : guest.getId();
    }

    private Reservation resolveReservation(UUID id) {
        Reservation reservation = reservationService.findById(id);
        if (isGuest()) {
            Guest guest = guestService.findByUserId(SecurityUtils.currentUserId());
            if (guest == null || !guest.getId().equals(reservation.getGuestId())) {
                throw new ResourceNotFoundException("Reservation", id);
            }
        }
        return reservation;
    }
}
