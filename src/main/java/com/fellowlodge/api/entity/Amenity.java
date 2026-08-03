package com.fellowlodge.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "amenities")
@Getter
@Setter
public class Amenity {

    @Id
    private UUID id;

    @Column(unique = true, nullable = false)
    private String name;

    private String icon;

    private String description;
}
