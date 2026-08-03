package com.fellowlodge.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "conference_halls")
@Getter
@Setter
public class ConferenceHall extends AuditableEntity {

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    private int capacity;

    @Column(name = "hourly_rate")
    private BigDecimal hourlyRate = BigDecimal.ZERO;

    @Column(name = "daily_rate")
    private BigDecimal dailyRate = BigDecimal.ZERO;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(length = 1000)
    private String amenities;

    @Column(name = "is_active")
    private boolean active = true;
}
