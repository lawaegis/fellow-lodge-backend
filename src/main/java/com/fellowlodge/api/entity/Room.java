package com.fellowlodge.api.entity;

import com.fellowlodge.api.enums.RoomStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "rooms")
@Getter
@Setter
public class Room extends AuditableEntity {

    @Column(name = "room_number", unique = true, nullable = false)
    private String roomNumber;

    @Column(name = "room_type_id")
    private UUID roomTypeId;

    private int floor = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomStatus status = RoomStatus.Available;

    @Column(name = "price_per_night", nullable = false)
    private BigDecimal pricePerNight;

    @Column(name = "extra_charges")
    private BigDecimal extraCharges = BigDecimal.ZERO;

    @Column(length = 2000)
    private String description;

    @Column(name = "has_balcony")
    private boolean hasBalcony;

    @Column(name = "has_view")
    private boolean hasView;

    @Column(name = "is_smoking")
    private boolean isSmoking;

    @Column(name = "is_accessible")
    private boolean isAccessible;

    @Column(length = 2000)
    private String notes;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "last_maintained")
    private LocalDateTime lastMaintained;

    public boolean isAvailable() {
        return status == RoomStatus.Available;
    }
}
