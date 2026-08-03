package com.fellowlodge.api.entity;

import com.fellowlodge.api.enums.PaymentMethod;
import com.fellowlodge.api.enums.PaymentStatus;
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
@Table(name = "payments")
@Getter
@Setter
public class Payment extends AuditableEntity {

    @Column(name = "reservation_id")
    private UUID reservationId;

    @Column(name = "guest_id")
    private UUID guestId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.Pending;

    @Column(name = "reference_number")
    private String referenceNumber;

    private String description;

    @Column(name = "received_by")
    private UUID receivedBy;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate = LocalDateTime.now();
}
