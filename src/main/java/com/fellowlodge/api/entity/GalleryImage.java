package com.fellowlodge.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "gallery_images")
@Getter
@Setter
public class GalleryImage extends CreatedEntity {

    private String title;

    @Column(nullable = false)
    private String url;

    @Column(length = 1000)
    private String description;

    private String category;

    @Column(name = "is_active")
    private boolean active = true;
}
