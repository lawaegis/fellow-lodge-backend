package com.fellowlodge.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "booking_items")
@Getter
@Setter
public class BookingItem extends CreatedEntity {

    @Column(name = "cart_id", nullable = false)
    private UUID cartId;

    @Column(name = "room_id")
    private UUID roomId;

    @Column(name = "room_type_id")
    private UUID roomTypeId;

    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;

    @Column(name = "check_out_date", nullable = false)
    private LocalDate checkOutDate;

    @Column(name = "number_of_guests")
    private int numberOfGuests = 1;

    private int quantity = 1;

    @Column(name = "price_per_night", nullable = false)
    private BigDecimal pricePerNight;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;
}
