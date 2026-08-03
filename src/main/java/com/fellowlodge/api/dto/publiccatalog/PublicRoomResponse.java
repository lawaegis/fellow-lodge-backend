package com.fellowlodge.api.dto.publiccatalog;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Public room view for the guest portal. Includes every image linked to the
 * room so the portal renders a live gallery without a second call, plus the
 * room-type details (base price, capacity, amenities).
 */
public record PublicRoomResponse(
        UUID id,
        String roomNumber,
        int floor,
        String status,
        BigDecimal pricePerNight,
        BigDecimal extraCharges,
        String description,
        boolean hasBalcony,
        boolean hasView,
        boolean smoking,
        boolean accessible,
        String imageUrl,
        List<String> images,
        PublicRoomTypeResponse roomType) {
}
