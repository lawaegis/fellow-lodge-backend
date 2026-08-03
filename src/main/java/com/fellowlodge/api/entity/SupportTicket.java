package com.fellowlodge.api.entity;

import com.fellowlodge.api.enums.SupportTicketPriority;
import com.fellowlodge.api.enums.SupportTicketStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "support_tickets")
@Getter
@Setter
public class SupportTicket extends CreatedEntity {

    @Column(name = "ticket_number", nullable = false, unique = true, length = 30)
    private String ticketNumber;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "guest_id")
    private UUID guestId;

    @Column(nullable = false, length = 200)
    private String subject;

    @Column(nullable = false, length = 4000)
    private String message;

    @Column(length = 100)
    private String category = "General";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SupportTicketPriority priority = SupportTicketPriority.Medium;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SupportTicketStatus status = SupportTicketStatus.Open;
}
