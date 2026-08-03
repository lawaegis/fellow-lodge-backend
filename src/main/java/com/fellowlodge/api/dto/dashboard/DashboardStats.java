package com.fellowlodge.api.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats {

    private long totalRooms;
    private long availableRooms;
    private long occupiedRooms;
    private long reservedRooms;
    private long maintenanceRooms;
    private long cleaningRooms;
    private double occupancyRate;
    private long totalGuests;
    private long vipGuests;
    private long totalReservations;
    private long confirmedReservations;
    private long pendingReservations;
    private long checkedInReservations;
    private long cancelledReservations;
    private long todayCheckIns;
    private long todayCheckOuts;
    private long todayBookings;
    private long pendingMaintenance;
    private long pendingHousekeeping;
    private long unreadNotifications;
    private BigDecimal todayRevenue;
    private BigDecimal weeklyRevenue;
    private BigDecimal monthlyRevenue;
    private BigDecimal totalRevenue;
    private Map<String, BigDecimal> revenueByPaymentMethod;
    private Map<String, Long> reservationsByStatus;
}
