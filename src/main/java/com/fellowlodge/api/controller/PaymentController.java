package com.fellowlodge.api.controller;

import com.fellowlodge.api.common.ApiResponse;
import com.fellowlodge.api.common.PageResponse;
import com.fellowlodge.api.dto.portal.PaymentIntentRequest;
import com.fellowlodge.api.entity.Payment;
import com.fellowlodge.api.security.SecurityUtils;
import com.fellowlodge.api.service.GuestService;
import com.fellowlodge.api.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final GuestService guestService;

    @GetMapping
    @PreAuthorize("hasAuthority('PAYMENTS:READ')")
    public ApiResponse<List<Payment>> findAll(@RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "20") int size,
                                              @RequestParam(required = false) String sort,
                                              @RequestParam(required = false) String search,
                                              @RequestParam(required = false) String status,
                                              @RequestParam(required = false) UUID reservationId,
                                              @RequestParam(required = false) UUID guestId,
                                              @RequestParam(required = false)
                                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                              @RequestParam(required = false)
                                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        UUID effectiveGuestId = isGuest() ? currentGuestId() : guestId;
        Page<Payment> result = paymentService.findAll(page, size, sort, search, status,
                reservationId, effectiveGuestId, from, to);
        return ApiResponse.ok(result.getContent(), PageResponse.from(result));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('PAYMENTS:READ')")
    public ApiResponse<List<Payment>> findAll() {
        if (isGuest()) {
            return ApiResponse.ok(paymentService.findByGuestId(currentGuestId()));
        }
        return ApiResponse.ok(paymentService.findAll());
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('PAYMENTS:READ')")
    public ApiResponse<Map<String, BigDecimal>> summary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ApiResponse.ok(Map.of("revenue", paymentService.sumCompletedBetween(from, to)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PAYMENTS:READ')")
    public ApiResponse<Payment> findById(@PathVariable UUID id) {
        return ApiResponse.ok(resolvePayment(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PAYMENTS:WRITE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Payment> create(@Valid @RequestBody Payment payment) {
        return ApiResponse.created("Payment recorded",
                paymentService.create(payment, SecurityUtils.currentUserId()));
    }

    @PostMapping("/intents")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Payment> createIntent(@Valid @RequestBody PaymentIntentRequest request) {
        return ApiResponse.created("Payment intent created",
                paymentService.createIntent(request, SecurityUtils.currentUserId()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PAYMENTS:WRITE')")
    public ApiResponse<Payment> update(@PathVariable UUID id, @Valid @RequestBody Payment payment) {
        return ApiResponse.ok("Payment updated", paymentService.update(id, payment));
    }

    @PostMapping("/{id}/refund")
    @PreAuthorize("hasAuthority('PAYMENTS:REFUND')")
    public ApiResponse<Payment> refund(@PathVariable UUID id, @RequestBody(required = false) Map<String, String> body) {
        String reason = body == null ? null : body.get("reason");
        return ApiResponse.ok("Payment refunded", paymentService.refund(id, reason));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PAYMENTS:WRITE')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        paymentService.delete(id);
        return ApiResponse.deleted("Payment deleted");
    }

    private Payment resolvePayment(UUID id) {
        Payment payment = paymentService.findById(id);
        if (isGuest() && !payment.getGuestId().equals(currentGuestId())) {
            throw new com.fellowlodge.api.common.exception.ResourceNotFoundException("Payment", id);
        }
        return payment;
    }

    private boolean isGuest() {
        return "Guest".equals(SecurityUtils.currentRole());
    }

    private UUID currentGuestId() {
        UUID userId = SecurityUtils.currentUserId();
        var guest = guestService.findByUserId(userId);
        return guest == null ? null : guest.getId();
    }
}
