package com.fellowlodge.api.entity;

import com.fellowlodge.api.enums.EventBookingStatus;
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
@Table(name = "event_bookings")
@Getter
@Setter
public class EventBooking extends AuditableEntity {

    @Column(name = "event_id")
    private UUID eventId;

    @Column(name = "conference_hall_id")
    private UUID conferenceHallId;

    @Column(name = "guest_id")
    private UUID guestId;

    @Column(name = "booked_by")
    private UUID bookedBy;

    @Column(name = "booking_date")
    private LocalDateTime bookingDate = LocalDateTime.now();

    @Column(name = "event_date")
    private LocalDateTime eventDate;

    @Column(name = "number_of_attendees")
    private int numberOfAttendees = 1;

    private BigDecimal amount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventBookingStatus status = EventBookingStatus.Pending;

    @Column(length = 2000)
    private String notes;
}
