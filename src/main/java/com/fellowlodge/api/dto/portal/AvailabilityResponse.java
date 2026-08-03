package com.fellowlodge.api.dto.portal;

import java.util.UUID;

public record AvailabilityResponse(
        boolean available,
        int capacity,
        int booked,
        int remaining,
        UUID id,
        String name
) {
}
