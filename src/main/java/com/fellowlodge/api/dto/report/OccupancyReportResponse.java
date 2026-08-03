package com.fellowlodge.api.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OccupancyReportResponse {

    private LocalDate from;
    private LocalDate to;
    private long totalRoomNights;
    private long occupiedRoomNights;
    private double occupancyRate;
    private BigDecimal revenuePerAvailableRoom;
    private long totalReservations;
    private BigDecimal averageDailyRate;
}
