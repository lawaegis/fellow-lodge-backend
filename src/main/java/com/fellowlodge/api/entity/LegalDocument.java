package com.fellowlodge.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "legal_documents")
@Getter
@Setter
public class LegalDocument extends AuditableEntity implements Activatible {

    @Column(nullable = false, unique = true, length = 150)
    private String slug;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 10000)
    private String content;

    @Column(length = 100)
    private String category = "Legal";

    @Column(length = 30)
    private String version = "1.0";

    @Column(name = "is_active")
    private boolean active = true;
}
