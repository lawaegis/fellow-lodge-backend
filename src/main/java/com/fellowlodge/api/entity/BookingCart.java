package com.fellowlodge.api.entity;

import com.fellowlodge.api.enums.BookingCartStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "booking_cart")
@Getter
@Setter
public class BookingCart extends AuditableEntity {

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "guest_id")
    private UUID guestId;

    @Column(name = "session_id")
    private String sessionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingCartStatus status = BookingCartStatus.Active;
}
