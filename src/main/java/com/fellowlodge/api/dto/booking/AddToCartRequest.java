package com.fellowlodge.api.dto.booking;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AddToCartRequest(
        @NotEmpty(message = "At least one item is required")
        List<@Valid CartItemRequest> items
) {
}
