package com.fellowlodge.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "attractions")
@Getter
@Setter
public class Attraction extends AuditableEntity implements Activatible {

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 3000)
    private String description;

    @Column(length = 100)
    private String category = "Sightseeing";

    @Column(length = 500)
    private String address;

    @Column(name = "distance_km")
    private BigDecimal distanceKm = BigDecimal.ZERO;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "sort_order")
    private int sortOrder;

    @Column(name = "is_active")
    private boolean active = true;
}
