package com.fellowlodge.api.dto.publiccatalog;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Public amenity view for the guest portal. Amenities are served from the
 * hotel services catalog (the single source of truth) so the portal never
 * shows an empty list even before the dedicated amenities module is used.
 */
public record PublicAmenityResponse(
        UUID id,
        String name,
        String description,
        String icon,
        String category,
        BigDecimal price,
        Integer durationMinutes,
        String imageUrl) {
}
