package com.fellowlodge.api.service;

import com.fellowlodge.api.dto.report.OccupancyReportResponse;
import com.fellowlodge.api.dto.report.RevenueReportResponse;
import com.fellowlodge.api.entity.Payment;
import com.fellowlodge.api.entity.Reservation;
import com.fellowlodge.api.enums.PaymentStatus;
import com.fellowlodge.api.enums.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final PaymentService paymentService;
    private final TransactionService transactionService;
    private final ReservationService reservationService;
    private final RoomService roomService;

    public RevenueReportResponse revenueReport(LocalDate from, LocalDate to) {
        LocalDate start = from == null ? LocalDate.now().withDayOfMonth(1) : from;
        LocalDate end = to == null ? LocalDate.now() : to;

        BigDecimal totalRevenue = paymentService.sumCompletedBetween(start, end);
        BigDecimal totalRefunds = transactionService.sumByTypeBetween(TransactionType.REFUND, start, end);
        BigDecimal netRevenue = totalRevenue.subtract(totalRefunds);

        List<Payment> payments = paymentService.findByDateRange(start, end);
        long paymentCount = payments.stream()
                .filter(p -> p.getPaymentStatus() == PaymentStatus.Completed)
                .count();
        long refundCount = transactionService.findAll(0, 1, null, TransactionType.REFUND.name(), null, start, end)
                .getTotalElements();

        Map<String, BigDecimal> revenueByMethod = new LinkedHashMap<>();
        payments.stream()
                .filter(p -> p.getPaymentStatus() == PaymentStatus.Completed)
                .forEach(p -> revenueByMethod.merge(p.getPaymentMethod().name(), p.getAmount(), BigDecimal::add));

        Map<String, Long> bookingsByStatus = new LinkedHashMap<>();
        reservationsInPeriod(start, end).forEach(r ->
                bookingsByStatus.merge(r.getStatus().name(), 1L, Long::sum));

        return RevenueReportResponse.builder()
                .from(start)
                .to(end)
                .totalRevenue(totalRevenue)
                .totalRefunds(totalRefunds)
                .netRevenue(netRevenue)
                .paymentCount(paymentCount)
                .refundCount(refundCount)
                .revenueByPaymentMethod(revenueByMethod)
                .bookingsByStatus(bookingsByStatus)
                .build();
    }

    public OccupancyReportResponse occupancyReport(LocalDate from, LocalDate to) {
        LocalDate start = from == null ? LocalDate.now().withDayOfMonth(1) : from;
        LocalDate end = to == null ? LocalDate.now() : to;
        long nightsInPeriod = Math.max(1, ChronoUnit.DAYS.between(start, end));

        List<Reservation> reservations = reservationsInPeriod(start, end);
        long totalReservations = reservations.size();
        long occupiedNights = reservations.stream()
                .mapToLong(r -> Math.max(1, ChronoUnit.DAYS.between(r.getCheckInDate(), r.getCheckOutDate())))
                .sum();

        long totalRooms = roomService.count();
        long availableRoomNights = totalRooms * nightsInPeriod;
        BigDecimal revenue = paymentService.sumCompletedBetween(start, end);

        double occupancyRate = availableRoomNights == 0 ? 0
                : Math.round(occupiedNights * 10000.0 / availableRoomNights) / 100.0;
        BigDecimal adr = occupiedNights == 0 ? BigDecimal.ZERO
                : revenue.divide(BigDecimal.valueOf(occupiedNights), 2, RoundingMode.HALF_UP);
        BigDecimal revPar = availableRoomNights == 0 ? BigDecimal.ZERO
                : revenue.divide(BigDecimal.valueOf(availableRoomNights), 2, RoundingMode.HALF_UP);

        return OccupancyReportResponse.builder()
                .from(start)
                .to(end)
                .totalRoomNights(availableRoomNights)
                .occupiedRoomNights(occupiedNights)
                .occupancyRate(occupancyRate)
                .revenuePerAvailableRoom(revPar)
                .totalReservations(totalReservations)
                .averageDailyRate(adr)
                .build();
    }

    private List<Reservation> reservationsInPeriod(LocalDate from, LocalDate to) {
        return reservationService.findAll().stream()
                .filter(r -> !r.getCheckInDate().isAfter(to) && !r.getCheckOutDate().isBefore(from))
                .toList();
    }
}
