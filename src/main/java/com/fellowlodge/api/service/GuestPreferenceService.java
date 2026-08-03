package com.fellowlodge.api.service;

import com.fellowlodge.api.entity.Guest;
import com.fellowlodge.api.entity.GuestPreference;
import com.fellowlodge.api.repository.GuestPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Per-guest stay preferences exposed to the guest portal. Each portal user has
 * at most one preference record (unique user_id).
 */
@Service
@RequiredArgsConstructor
public class GuestPreferenceService {

    private final GuestPreferenceRepository preferenceRepository;
    private final GuestService guestService;

    public GuestPreference findByUserId(UUID userId) {
        return preferenceRepository.findByUserId(userId).orElse(null);
    }

    @Transactional
    public GuestPreference upsert(UUID userId, GuestPreference incoming) {
        Guest guest = guestService.findByUserId(userId);
        GuestPreference existing = preferenceRepository.findByUserId(userId)
                .orElseGet(GuestPreference::new);
        existing.setUserId(userId);
        existing.setGuestId(guest == null ? null : guest.getId());
        existing.setRoomType(incoming.getRoomType());
        existing.setFloorPref(incoming.getFloorPref());
        existing.setBedType(incoming.getBedType());
        existing.setDietaryPref(incoming.getDietaryPref());
        existing.setAccessibility(incoming.getAccessibility());
        existing.setContactPref(incoming.getContactPref());
        existing.setNewsletter(incoming.isNewsletter());
        return preferenceRepository.save(existing);
    }
}
