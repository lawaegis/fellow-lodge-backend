package com.fellowlodge.api.dto.portal;

import java.util.List;

public record HotelInfoResponse(
        String name,
        String tagline,
        String logoUrl,
        String heroImage,
        String backgroundImage,
        String phone,
        String email,
        String address,
        String currencyCode,
        String currencySymbol,
        String checkInTime,
        String checkOutTime,
        String about,
        String mapUrl
) {
}
