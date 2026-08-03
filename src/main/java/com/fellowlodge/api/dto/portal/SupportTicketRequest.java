package com.fellowlodge.api.dto.portal;

import jakarta.validation.constraints.NotBlank;

public record SupportTicketRequest(
        @NotBlank(message = "Subject is required")
        String subject,
        @NotBlank(message = "Message is required")
        String message,
        String category,
        String priority
) {
}
