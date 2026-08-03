package com.fellowlodge.api.entity;

import com.fellowlodge.api.enums.ReservationSource;
import com.fellowlodge.api.enums.ReservationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reservations")
@Getter
@Setter
public class Reservation extends AuditableEntity {

    @Column(name = "guest_id")
    private UUID guestId;

    @Column(name = "room_id")
    private UUID roomId;

    @Column(name = "room_type_id")
    private UUID roomTypeId;

    @Column(name = "booked_by")
    private UUID bookedBy;

    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;

    @Column(name = "check_out_date", nullable = false)
    private LocalDate checkOutDate;

    @Column(name = "actual_check_in")
    private LocalDateTime actualCheckIn;

    @Column(name = "actual_check_out")
    private LocalDateTime actualCheckOut;

    @Column(name = "number_of_guests")
    private int numberOfGuests = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status = ReservationStatus.Pending;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Column(name = "discount_percent")
    private BigDecimal discountPercent = BigDecimal.ZERO;

    @Column(name = "special_requests", length = 2000)
    private String specialRequests;

    @Column(name = "cancellation_reason", length = 1000)
    private String cancellationReason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationSource source = ReservationSource.DESKTOP;
}
