package com.fellowlodge.api.dto.booking;

import jakarta.validation.constraints.NotBlank;

public record CancelRequest(
        @NotBlank(message = "Cancellation reason is required")
        String reason
) {
}
