package com.fellowlodge.api.dto.booking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {

    private UUID itemId;
    private UUID roomTypeId;
    private String roomTypeName;
    private UUID roomId;
    private String roomNumber;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private int numberOfGuests;
    private int quantity;
    private int nights;
    private BigDecimal pricePerNight;
    private BigDecimal totalAmount;
}
