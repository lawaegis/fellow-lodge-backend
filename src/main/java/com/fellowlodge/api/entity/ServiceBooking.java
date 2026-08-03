package com.fellowlodge.api.entity;

import com.fellowlodge.api.enums.ServiceBookingStatus;
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
@Table(name = "service_bookings")
@Getter
@Setter
public class ServiceBooking extends AuditableEntity {

    @Column(name = "guest_id")
    private UUID guestId;

    @Column(name = "service_id")
    private UUID serviceId;

    @Column(name = "reservation_id")
    private UUID reservationId;

    @Column(name = "booking_date")
    private LocalDateTime bookingDate = LocalDateTime.now();

    @Column(name = "service_time")
    private LocalDateTime serviceTime;

    private int quantity = 1;

    @Column(name = "total_amount")
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceBookingStatus status = ServiceBookingStatus.Pending;

    @Column(length = 2000)
    private String notes;
}
