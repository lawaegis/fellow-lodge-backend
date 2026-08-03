package com.fellowlodge.api.entity;

import com.fellowlodge.api.enums.MaintenanceStatus;
import com.fellowlodge.api.enums.PriorityLevel;
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
@Table(name = "maintenance")
@Getter
@Setter
public class Maintenance extends AuditableEntity {

    @Column(name = "room_id")
    private UUID roomId;

    @Column(name = "reported_by")
    private UUID reportedBy;

    @Column(name = "assigned_to")
    private UUID assignedTo;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false, length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PriorityLevel priority = PriorityLevel.Medium;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MaintenanceStatus status = MaintenanceStatus.Reported;

    @Column(name = "estimated_cost")
    private BigDecimal estimatedCost = BigDecimal.ZERO;

    @Column(name = "actual_cost")
    private BigDecimal actualCost;

    @Column(name = "reported_date")
    private LocalDateTime reportedDate = LocalDateTime.now();

    @Column(name = "completed_date")
    private LocalDateTime completedDate;
}
