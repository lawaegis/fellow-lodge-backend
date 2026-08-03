package com.fellowlodge.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "menu_items")
@Getter
@Setter
public class MenuItem extends AuditableEntity implements Activatible {

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private BigDecimal price = BigDecimal.ZERO;

    @Column(name = "category_id")
    private UUID categoryId;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(length = 2000)
    private String ingredients;

    @Column(name = "is_available")
    private boolean available = true;

    @Column(name = "is_active")
    private boolean active = true;
}
