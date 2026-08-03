package com.fellowlodge.api.dto.portal;

import java.math.BigDecimal;

public record PromoValidateResponse(
        boolean valid,
        String code,
        BigDecimal discountPercent,
        BigDecimal discountAmount,
        String message
) {
}
