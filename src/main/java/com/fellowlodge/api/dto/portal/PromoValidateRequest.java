package com.fellowlodge.api.dto.portal;

import jakarta.validation.constraints.NotBlank;

public record PromoValidateRequest(
        @NotBlank(message = "Promotion code is required")
        String code,
        java.math.BigDecimal subtotal
) {
}
