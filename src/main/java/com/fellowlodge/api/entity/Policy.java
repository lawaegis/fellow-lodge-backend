package com.fellowlodge.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "policies")
@Getter
@Setter
public class Policy extends AuditableEntity implements Activatible {

    @Column(nullable = false)
    private String title;

    @Column(length = 100)
    private String category = "General";

    @Column(nullable = false, length = 4000)
    private String content;

    @Column(name = "sort_order")
    private int sortOrder;

    @Column(name = "is_active")
    private boolean active = true;
}
