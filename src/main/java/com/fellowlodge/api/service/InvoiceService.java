package com.fellowlodge.api.service;

import com.fellowlodge.api.common.exception.InvalidOperationException;
import com.fellowlodge.api.common.exception.ResourceNotFoundException;
import com.fellowlodge.api.entity.Invoice;
import com.fellowlodge.api.enums.InvoiceStatus;
import com.fellowlodge.api.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;

    public Page<Invoice> findAll(int page, int size, String sort, String search, String status,
                                 UUID reservationId, UUID guestId) {
        Pageable pageable = PageRequest.of(page, size, buildSort(sort));
        Specification<Invoice> spec = Specification.where(null);
        if (StringUtils.hasText(search)) {
            spec = spec.and((root, q, cb) -> cb.or(
                    cb.like(cb.lower(root.get("invoiceNumber")), "%" + search.toLowerCase() + "%"),
                    cb.like(cb.lower(cb.coalesce(root.get("notes"), "")), "%" + search.toLowerCase() + "%")));
        }
        if (StringUtils.hasText(status)) {
            InvoiceStatus is = InvoiceStatus.valueOf(status);
            spec = spec.and((root, q, cb) -> cb.equal(root.get("status"), is));
        }
        if (reservationId != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("reservationId"), reservationId));
        }
        if (guestId != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("guestId"), guestId));
        }
        return invoiceRepository.findAll(spec, pageable);
    }

    public List<Invoice> findAll() {
        return invoiceRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public Invoice findById(UUID id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", id));
    }

    public Invoice findByInvoiceNumber(String invoiceNumber) {
        return invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice with number " + invoiceNumber));
    }

    public List<Invoice> findByGuestId(UUID guestId) {
        return invoiceRepository.findByGuestId(guestId);
    }

    @Transactional
    public Invoice create(Invoice invoice, UUID issuedBy) {
        if (invoice.getInvoiceNumber() == null || invoice.getInvoiceNumber().isBlank()) {
            invoice.setInvoiceNumber(generateInvoiceNumber());
        }
        invoice.setIssuedBy(issuedBy);
        if (invoice.getTotalAmount() == null) {
            invoice.setTotalAmount(invoice.getSubtotal()
                    .add(invoice.getTaxAmount() == null ? java.math.BigDecimal.ZERO : invoice.getTaxAmount())
                    .subtract(invoice.getDiscountAmount() == null ? java.math.BigDecimal.ZERO : invoice.getDiscountAmount()));
        }
        return invoiceRepository.save(invoice);
    }

    @Transactional
    public Invoice update(UUID id, Invoice updated) {
        Invoice invoice = findById(id);
        if (invoice.getStatus() == InvoiceStatus.Paid) {
            throw new InvalidOperationException("Paid invoices cannot be modified.");
        }
        updated.setId(id);
        updated.setInvoiceNumber(invoice.getInvoiceNumber());
        return invoiceRepository.save(updated);
    }

    @Transactional
    public Invoice markPaid(UUID id) {
        Invoice invoice = findById(id);
        invoice.setStatus(InvoiceStatus.Paid);
        return invoiceRepository.save(invoice);
    }

    @Transactional
    public Invoice markCancelled(UUID id) {
        Invoice invoice = findById(id);
        if (invoice.getStatus() == InvoiceStatus.Paid) {
            throw new InvalidOperationException("Paid invoices cannot be cancelled.");
        }
        invoice.setStatus(InvoiceStatus.Cancelled);
        return invoiceRepository.save(invoice);
    }

    @Transactional
    public void delete(UUID id) {
        invoiceRepository.delete(findById(id));
    }

    public long count() {
        return invoiceRepository.count();
    }

    private String generateInvoiceNumber() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String candidate;
        do {
            candidate = "INV-" + date + "-" + ThreadLocalRandom.current().nextInt(10000, 99999);
        } while (invoiceRepository.findByInvoiceNumber(candidate).isPresent());
        return candidate;
    }

    private Sort buildSort(String sort) {
        if (!StringUtils.hasText(sort)) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        String[] parts = sort.split(",");
        Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1])
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, parts[0]);
    }
}
