package com.fellowlodge.api.entity;

import com.fellowlodge.api.enums.HousekeepingStatus;
import com.fellowlodge.api.enums.HousekeepingTaskType;
import com.fellowlodge.api.enums.PriorityLevel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "housekeeping")
@Getter
@Setter
public class Housekeeping extends AuditableEntity {

    @Column(name = "room_id")
    private UUID roomId;

    @Column(name = "assigned_to")
    private UUID assignedTo;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_type", nullable = false)
    private HousekeepingTaskType taskType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HousekeepingStatus status = HousekeepingStatus.Pending;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PriorityLevel priority = PriorityLevel.Medium;

    @Column(length = 2000)
    private String description;

    @Column(length = 2000)
    private String notes;

    @Column(name = "scheduled_date")
    private LocalDate scheduledDate;

    @Column(name = "completed_date")
    private LocalDateTime completedDate;
}
