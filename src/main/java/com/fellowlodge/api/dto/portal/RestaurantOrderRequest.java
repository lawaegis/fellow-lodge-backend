package com.fellowlodge.api.dto.portal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record RestaurantOrderRequest(
        @NotEmpty(message = "Order must contain at least one item")
        List<RestaurantOrderItemRequest> items,
        String orderType,
        String specialRequests,
        String guestName,
        String guestEmail,
        String guestPhone
) {
}
