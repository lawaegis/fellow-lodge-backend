package com.fellowlodge.api.dto.booking;

import jakarta.validation.constraints.NotBlank;

public record CheckoutRequest(
        java.util.UUID cartId,
        String promoCode,
        String paymentMethod,
        String specialRequests,
        @NotBlank(message = "Guest name is required")
        String guestName,
        String guestEmail,
        String guestPhone
) {
}
