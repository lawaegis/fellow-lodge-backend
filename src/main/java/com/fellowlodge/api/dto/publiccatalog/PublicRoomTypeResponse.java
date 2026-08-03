package com.fellowlodge.api.dto.publiccatalog;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Public room-type view for the guest portal: pricing, capacity and amenities.
 */
public record PublicRoomTypeResponse(
        UUID id,
        String name,
        String description,
        BigDecimal basePrice,
        int maxGuests,
        String bedType,
        BigDecimal sizeSqm,
        List<String> amenities,
        String imageUrl) {
}
