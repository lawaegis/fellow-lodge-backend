package com.fellowlodge.api.repository;

import com.fellowlodge.api.entity.GuestPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface GuestPreferenceRepository extends JpaRepository<GuestPreference, UUID> {

    Optional<GuestPreference> findByUserId(UUID userId);

    Optional<GuestPreference> findByGuestId(UUID guestId);
}
