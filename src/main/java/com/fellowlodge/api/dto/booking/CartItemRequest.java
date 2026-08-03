package com.fellowlodge.api.dto.booking;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record CartItemRequest(
        UUID roomTypeId,
        UUID roomId,
        @NotNull(message = "Check-in date is required")
        @FutureOrPresent(message = "Check-in date must be today or in the future")
        LocalDate checkInDate,
        @NotNull(message = "Check-out date is required")
        LocalDate checkOutDate,
        @Min(value = 1, message = "At least one guest is required")
        int numberOfGuests,
        @Min(value = 1, message = "Quantity must be at least 1")
        @Max(value = 10, message = "Quantity cannot exceed 10")
        int quantity
) {
}
