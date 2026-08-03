package com.fellowlodge.api.repository;

import com.fellowlodge.api.entity.Payment;
import com.fellowlodge.api.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID>, JpaSpecificationExecutor<Payment> {

    List<Payment> findByReservationId(UUID reservationId);

    List<Payment> findByGuestId(UUID guestId);

    List<Payment> findByPaymentStatus(PaymentStatus status);

    java.util.Optional<Payment> findByReferenceNumber(String referenceNumber);

    List<Payment> findByPaymentDateBetween(LocalDateTime start, LocalDateTime end);

    @Query("select coalesce(sum(p.amount), 0) from Payment p where p.paymentStatus = :status")
    BigDecimal sumByPaymentStatus(@Param("status") PaymentStatus status);

    @Query("""
            select coalesce(sum(p.amount), 0) from Payment p
            where p.paymentStatus = com.fellowlodge.api.enums.PaymentStatus.Completed
              and p.paymentDate between :start and :end
            """)
    BigDecimal sumCompletedPaymentsBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
