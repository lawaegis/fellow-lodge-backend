package com.fellowlodge.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "check_outs")
@Getter
@Setter
public class CheckOut extends CreatedEntity {

    @Column(name = "check_in_id")
    private UUID checkInId;

    @Column(name = "checked_out_by")
    private UUID checkedOutBy;

    @Column(name = "check_out_time")
    private LocalDateTime checkOutTime = LocalDateTime.now();

    @Column(name = "room_condition")
    private String roomCondition = "Good";

    @Column(name = "mini_bar_charges")
    private BigDecimal miniBarCharges = BigDecimal.ZERO;

    @Column(name = "damage_charges")
    private BigDecimal damageCharges = BigDecimal.ZERO;

    @Column(name = "other_charges")
    private BigDecimal otherCharges = BigDecimal.ZERO;

    @Column(name = "total_additional_charges")
    private BigDecimal totalAdditionalCharges = BigDecimal.ZERO;

    @Column(length = 2000)
    private String notes;
}
