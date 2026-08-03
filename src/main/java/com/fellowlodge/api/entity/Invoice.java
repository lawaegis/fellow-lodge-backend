package com.fellowlodge.api.entity;

import com.fellowlodge.api.enums.InvoiceStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "invoices")
@Getter
@Setter
public class Invoice extends AuditableEntity {

    @Column(name = "reservation_id")
    private UUID reservationId;

    @Column(name = "guest_id")
    private UUID guestId;

    @Column(name = "invoice_number", unique = true, nullable = false)
    private String invoiceNumber;

    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "tax_amount")
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "discount_amount")
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceStatus status = InvoiceStatus.Draft;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "issued_by")
    private UUID issuedBy;

    @Column(length = 2000)
    private String notes;
}
