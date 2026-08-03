package com.fellowlodge.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "settings")
@Getter
@Setter
public class Setting extends AuditableEntity {

    @Column(name = "\"key\"", unique = true, nullable = false)
    private String key;

    @Column(name = "\"value\"", length = 2000)
    private String value;

    private String category = "general";

    private String description;
}
