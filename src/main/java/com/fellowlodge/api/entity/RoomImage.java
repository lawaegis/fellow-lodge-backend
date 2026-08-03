package com.fellowlodge.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "room_images")
@Getter
@Setter
public class RoomImage extends CreatedEntity {

    @Column(name = "room_id")
    private UUID roomId;

    @Column(name = "room_type_id")
    private UUID roomTypeId;

    @Column(nullable = false)
    private String url;

    private String caption;

    @Column(name = "is_primary")
    private boolean primary;

    @Column(name = "sort_order")
    private int sortOrder;
}
