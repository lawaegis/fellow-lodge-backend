package com.fellowlodge.api.dto.checkin;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CheckInRequest(
        @NotNull(message = "Reservation is required")
        UUID reservationId,
        String roomCondition,
        String notes
) {
}
