package com.fellowlodge.api.dto.booking;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record BookingRequest(
        @NotNull(message = "Guest is required")
        UUID guestId,
        UUID roomTypeId,
        UUID roomId,
        @NotNull(message = "Check-in date is required")
        @FutureOrPresent(message = "Check-in date must be today or in the future")
        LocalDate checkInDate,
        @NotNull(message = "Check-out date is required")
        LocalDate checkOutDate,
        @Min(value = 1, message = "At least one guest is required")
        int numberOfGuests,
        String specialRequests,
        String source
) {
}
