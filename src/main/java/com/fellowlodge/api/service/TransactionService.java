package com.fellowlodge.api.service;

import com.fellowlodge.api.common.exception.ResourceNotFoundException;
import com.fellowlodge.api.entity.Transaction;
import com.fellowlodge.api.enums.TransactionType;
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
import java.time.LocalTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public Page<Transaction> findAll(int page, int size, String sort, String type, UUID guestId,
                                     LocalDate from, LocalDate to) {
        Pageable pageable = PageRequest.of(page, size, buildSort(sort));
        Specification<Transaction> spec = Specification.where(null);
        if (StringUtils.hasText(type)) {
            TransactionType tt = TransactionType.valueOf(type);
            spec = spec.and((root, q, cb) -> cb.equal(root.get("transactionType"), tt));
        }
        if (guestId != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("guestId"), guestId));
        }
        if (from != null) {
            spec = spec.and((root, q, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), from.atStartOfDay()));
        }
        if (to != null) {
            spec = spec.and((root, q, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), to.atTime(LocalTime.MAX)));
        }
        return transactionRepository.findAll(spec, pageable);
    }

    public Transaction findById(UUID id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", id));
    }

    @Transactional
    public Transaction create(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    public BigDecimal sumByType(TransactionType type) {
        return transactionRepository.sumByType(type);
    }

    public BigDecimal sumByTypeBetween(TransactionType type, LocalDate from, LocalDate to) {
        return transactionRepository.sumByTypeBetween(type, from.atStartOfDay(), to.atTime(LocalTime.MAX));
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
