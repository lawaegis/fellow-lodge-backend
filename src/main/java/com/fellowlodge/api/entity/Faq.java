package com.fellowlodge.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "faqs")
@Getter
@Setter
public class Faq extends AuditableEntity implements Activatible {

    @Column(nullable = false, length = 500)
    private String question;

    @Column(nullable = false, length = 3000)
    private String answer;

    @Column(length = 100)
    private String category = "General";

    @Column(name = "sort_order")
    private int sortOrder;

    @Column(name = "is_active")
    private boolean active = true;
}
