package com.fellowlodge.api.entity;

import com.fellowlodge.api.enums.NotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "notifications")
@Getter
@Setter
public class Notification extends CreatedEntity {

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "guest_id")
    private UUID guestId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 2000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type = NotificationType.Info;

    @Column(name = "is_read")
    private boolean read;

    @Column(name = "action_url")
    private String actionUrl;
}
