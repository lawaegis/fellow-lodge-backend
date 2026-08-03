package com.fellowlodge.api.service;

import com.fellowlodge.api.common.exception.InvalidOperationException;
import com.fellowlodge.api.common.exception.ResourceNotFoundException;
import com.fellowlodge.api.dto.portal.PaymentIntentRequest;
import com.fellowlodge.api.entity.Payment;
import com.fellowlodge.api.entity.Transaction;
import com.fellowlodge.api.enums.PaymentMethod;
import com.fellowlodge.api.enums.PaymentStatus;
import com.fellowlodge.api.enums.TransactionStatus;
import com.fellowlodge.api.enums.TransactionType;
import com.fellowlodge.api.repository.PaymentRepository;
import com.fellowlodge.api.repository.ReservationRepository;
import com.fellowlodge.api.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final TransactionRepository transactionRepository;
    private final ReservationRepository reservationRepository;

    public Page<Payment> findAll(int page, int size, String sort, String search, String status,
                                 UUID reservationId, UUID guestId, LocalDate from, LocalDate to) {
        Pageable pageable = PageRequest.of(page, size, buildSort(sort));
        Specification<Payment> spec = Specification.where(null);
        if (StringUtils.hasText(search)) {
            spec = spec.and((root, q, cb) -> cb.or(
                    cb.like(cb.lower(root.get("referenceNumber")), "%" + search.toLowerCase() + "%"),
                    cb.like(cb.lower(cb.coalesce(root.get("description"), "")), "%" + search.toLowerCase() + "%")));
        }
        if (StringUtils.hasText(status)) {
            PaymentStatus ps = PaymentStatus.valueOf(status);
            spec = spec.and((root, q, cb) -> cb.equal(root.get("paymentStatus"), ps));
        }
        if (reservationId != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("reservationId"), reservationId));
        }
        if (guestId != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("guestId"), guestId));
        }
        if (from != null) {
            spec = spec.and((root, q, cb) -> cb.greaterThanOrEqualTo(root.get("paymentDate"), from.atStartOfDay()));
        }
        if (to != null) {
            spec = spec.and((root, q, cb) ->
                    cb.lessThanOrEqualTo(root.get("paymentDate"), to.atTime(LocalTime.MAX)));
        }
        return paymentRepository.findAll(spec, pageable);
    }

    public List<Payment> findAll() {
        return paymentRepository.findAll(Sort.by(Sort.Direction.DESC, "paymentDate"));
    }

    public Payment findById(UUID id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", id));
    }

    public List<Payment> findByReservationId(UUID reservationId) {
        return paymentRepository.findByReservationId(reservationId);
    }

    public List<Payment> findByGuestId(UUID guestId) {
        return paymentRepository.findByGuestId(guestId);
    }

    public List<Payment> findByDateRange(LocalDate start, LocalDate end) {
        return paymentRepository.findByPaymentDateBetween(start.atStartOfDay(), end.atTime(LocalTime.MAX));
    }

    @Transactional
    public Payment create(Payment payment, UUID receivedBy) {
        if (payment.getAmount() == null || payment.getAmount().signum() <= 0) {
            throw new InvalidOperationException("Payment amount must be greater than zero.");
        }
        if (payment.getPaymentStatus() == null) {
            payment.setPaymentStatus(PaymentStatus.Pending);
        }
        payment.setReceivedBy(receivedBy);
        if (payment.getPaymentDate() == null) {
            payment.setPaymentDate(LocalDateTime.now());
        }
        Payment saved = paymentRepository.save(payment);

        if (saved.getPaymentStatus() == PaymentStatus.Completed) {
            recordTransaction(saved, TransactionType.PAYMENT, "Payment received");
        }
        return saved;
    }

    @Transactional
    public Payment createIntent(PaymentIntentRequest request, UUID initiatedBy) {
        if (request.amount() == null || request.amount().signum() <= 0) {
            throw new InvalidOperationException("Payment amount must be greater than zero.");
        }
        Payment payment = new Payment();
        payment.setAmount(request.amount());
        payment.setReservationId(request.reservationId());
        payment.setPaymentMethod(request.method() == null || request.method().isBlank()
                ? PaymentMethod.Online : parseMethod(request.method()));
        payment.setPaymentStatus(PaymentStatus.Pending);
        payment.setDescription(request.description());
        payment.setReceivedBy(initiatedBy);
        payment.setReferenceNumber(generateReferenceNumber());
        payment.setPaymentDate(LocalDateTime.now());
        if (request.reservationId() != null) {
            reservationRepository.findById(request.reservationId())
                    .ifPresent(reservation -> payment.setGuestId(reservation.getGuestId()));
        }
        return paymentRepository.save(payment);
    }

    private PaymentMethod parseMethod(String value) {
        try {
            return PaymentMethod.valueOf(value);
        } catch (IllegalArgumentException ex) {
            return PaymentMethod.Online;
        }
    }

    private String generateReferenceNumber() {
        int random = ThreadLocalRandom.current().nextInt(100000, 999999);
        String candidate;
        do {
            candidate = "PAY-" + LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "-" + random;
        } while (paymentRepository.findByReferenceNumber(candidate).isPresent());
        return candidate;
    }

    @Transactional
    public Payment update(UUID id, Payment updated) {
        Payment payment = findById(id);
        if (payment.getPaymentStatus() == PaymentStatus.Refunded) {
            throw new InvalidOperationException("Refunded payments cannot be modified.");
        }
        updated.setId(id);
        return paymentRepository.save(updated);
    }

    @Transactional
    public Payment refund(UUID id, String reason) {
        Payment payment = findById(id);
        if (payment.getPaymentStatus() != PaymentStatus.Completed) {
            throw new InvalidOperationException("Only completed payments can be refunded.");
        }
        payment.setPaymentStatus(PaymentStatus.Refunded);
        Payment saved = paymentRepository.save(payment);
        recordTransaction(saved, TransactionType.REFUND, "Refund: " + (reason == null ? "Requested by guest" : reason));
        return saved;
    }

    @Transactional
    public void delete(UUID id) {
        paymentRepository.delete(findById(id));
    }

    public BigDecimal sumCompleted() {
        return paymentRepository.sumByPaymentStatus(PaymentStatus.Completed);
    }

    public BigDecimal sumCompletedBetween(LocalDate start, LocalDate end) {
        return paymentRepository.sumCompletedPaymentsBetween(start.atStartOfDay(), end.atTime(LocalTime.MAX));
    }

    private void recordTransaction(Payment payment, TransactionType type, String description) {
        Transaction transaction = new Transaction();
        transaction.setPaymentId(payment.getId());
        transaction.setGuestId(payment.getGuestId());
        transaction.setTransactionType(type);
        transaction.setAmount(payment.getAmount());
        transaction.setStatus(TransactionStatus.Completed);
        transaction.setReference(payment.getReferenceNumber());
        transaction.setDescription(description);
        transactionRepository.save(transaction);
    }

    private Sort buildSort(String sort) {
        if (!StringUtils.hasText(sort)) {
            return Sort.by(Sort.Direction.DESC, "paymentDate");
        }
        String[] parts = sort.split(",");
        Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1])
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, parts[0]);
    }
}
