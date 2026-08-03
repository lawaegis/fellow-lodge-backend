package com.fellowlodge.api.dto.checkin;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CheckOutRequest(
        @NotNull(message = "Check-in record is required")
        UUID checkInId,
        String roomCondition,
        BigDecimal miniBarCharges,
        BigDecimal damageCharges,
        BigDecimal otherCharges,
        String notes
) {
}
