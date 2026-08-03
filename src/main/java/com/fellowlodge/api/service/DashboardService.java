package com.fellowlodge.api.service;

import com.fellowlodge.api.dto.dashboard.DashboardStats;
import com.fellowlodge.api.entity.Payment;
import com.fellowlodge.api.enums.HousekeepingStatus;
import com.fellowlodge.api.enums.MaintenanceStatus;
import com.fellowlodge.api.enums.PaymentStatus;
import com.fellowlodge.api.enums.ReservationStatus;
import com.fellowlodge.api.enums.RoomStatus;
import com.fellowlodge.api.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final RoomService roomService;
    private final GuestService guestService;
    private final ReservationService reservationService;
    private final PaymentService paymentService;
    private final MaintenanceService maintenanceService;
    private final HousekeepingService housekeepingService;
    private final NotificationService notificationService;
    private final ReservationRepository reservationRepository;

    public DashboardStats getStats(UUID currentUserId) {
        LocalDate today = LocalDate.now();
        long totalRooms = roomService.count();
        long occupied = roomService.countByStatus(RoomStatus.Occupied);

        Map<String, BigDecimal> revenueByMethod = new LinkedHashMap<>();
        paymentService.findAll().stream()
                .filter(p -> p.getPaymentStatus() == PaymentStatus.Completed)
                .forEach(p -> revenueByMethod.merge(p.getPaymentMethod().name(), p.getAmount(), BigDecimal::add));

        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (ReservationStatus status : ReservationStatus.values()) {
            long count = reservationService.countByStatus(status);
            if (count > 0) {
                byStatus.put(status.name(), count);
            }
        }

        return DashboardStats.builder()
                .totalRooms(totalRooms)
                .availableRooms(roomService.countByStatus(RoomStatus.Available))
                .occupiedRooms(occupied)
                .reservedRooms(roomService.countByStatus(RoomStatus.Reserved))
                .maintenanceRooms(roomService.countByStatus(RoomStatus.Maintenance))
                .cleaningRooms(roomService.countByStatus(RoomStatus.Cleaning))
                .occupancyRate(totalRooms == 0 ? 0
                        : Math.round(occupied * 10000.0 / totalRooms) / 100.0)
                .totalGuests(guestService.count())
                .vipGuests(guestService.countVip())
                .totalReservations(reservationService.count())
                .confirmedReservations(reservationService.countByStatus(ReservationStatus.Confirmed))
                .pendingReservations(reservationService.countByStatus(ReservationStatus.Pending))
                .checkedInReservations(reservationService.countByStatus(ReservationStatus.CheckedIn))
                .cancelledReservations(reservationService.countByStatus(ReservationStatus.Cancelled))
                .todayCheckIns(reservationService.findTodayCheckIns().size())
                .todayCheckOuts(reservationService.findTodayCheckOuts().size())
                .todayBookings(countBookingsCreatedOn(today))
                .pendingMaintenance(maintenanceService.countByStatus(MaintenanceStatus.Reported)
                        + maintenanceService.countByStatus(MaintenanceStatus.InProgress))
                .pendingHousekeeping(housekeepingService.countByStatus(HousekeepingStatus.Pending))
                .unreadNotifications(currentUserId == null ? 0
                        : notificationService.countUnreadByUserId(currentUserId))
                .todayRevenue(paymentService.sumCompletedBetween(today, today))
                .weeklyRevenue(paymentService.sumCompletedBetween(today.minusDays(6), today))
                .monthlyRevenue(paymentService.sumCompletedBetween(today.withDayOfMonth(1), today))
                .totalRevenue(paymentService.sumCompleted())
                .revenueByPaymentMethod(revenueByMethod)
                .reservationsByStatus(byStatus)
                .build();
    }

    private long countBookingsCreatedOn(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        Specification<com.fellowlodge.api.entity.Reservation> spec =
                (root, query, cb) -> cb.between(root.get("createdAt"), start, end);
        return reservationRepository.count(spec);
    }
}
