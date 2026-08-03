package com.fellowlodge.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "guest_preferences")
@Getter
@Setter
public class GuestPreference extends AuditableEntity {

    @Column(name = "guest_id", nullable = false)
    private UUID guestId;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "room_type", length = 100)
    private String roomType;

    @Column(name = "floor_pref", length = 100)
    private String floorPref;

    @Column(name = "bed_type", length = 100)
    private String bedType;

    @Column(name = "dietary_pref", length = 500)
    private String dietaryPref;

    @Column(length = 500)
    private String accessibility;

    @Column(name = "contact_pref", length = 30)
    private String contactPref;

    private boolean newsletter;
}
