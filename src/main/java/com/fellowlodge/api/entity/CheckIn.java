package com.fellowlodge.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "check_ins")
@Getter
@Setter
public class CheckIn extends CreatedEntity {

    @Column(name = "reservation_id")
    private UUID reservationId;

    @Column(name = "room_id")
    private UUID roomId;

    @Column(name = "guest_id")
    private UUID guestId;

    @Column(name = "checked_in_by")
    private UUID checkedInBy;

    @Column(name = "check_in_time")
    private LocalDateTime checkInTime = LocalDateTime.now();

    @Column(name = "room_condition")
    private String roomCondition = "Good";

    @Column(length = 2000)
    private String notes;
}
