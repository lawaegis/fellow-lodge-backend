package com.fellowlodge.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "banners")
@Getter
@Setter
public class Banner extends AuditableEntity implements Activatible {

    @Column(nullable = false)
    private String title;

    @Column(length = 500)
    private String subtitle;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "link_url")
    private String linkUrl;

    @Column(length = 30)
    private String position = "home";

    @Column(name = "is_active")
    private boolean active = true;
}
