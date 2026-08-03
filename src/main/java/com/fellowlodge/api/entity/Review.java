package com.fellowlodge.api.entity;

import com.fellowlodge.api.enums.ReviewStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "reviews")
@Getter
@Setter
public class Review extends CreatedEntity {

    @Column(name = "guest_id")
    private UUID guestId;

    @Column(name = "room_id")
    private UUID roomId;

    @Column(name = "reservation_id")
    private UUID reservationId;

    @Column(nullable = false)
    private int rating;

    @Column(length = 2000)
    private String comment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewStatus status = ReviewStatus.Pending;
}
