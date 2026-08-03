package com.fellowlodge.api.dto.portal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record RestaurantOrderItemRequest(
        @NotNull(message = "Menu item id is required")
        UUID menuItemId,
        @Min(value = 1, message = "Quantity must be at least 1")
        int quantity,
        String notes
) {
}
