package com.fellowlodge.api.controller;

import com.fellowlodge.api.common.ApiResponse;
import com.fellowlodge.api.common.PageResponse;
import com.fellowlodge.api.entity.Transaction;
import com.fellowlodge.api.enums.TransactionType;
import com.fellowlodge.api.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    @PreAuthorize("hasAuthority('TRANSACTIONS:READ')")
    public ApiResponse<List<Transaction>> findAll(@RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "20") int size,
                                                  @RequestParam(required = false) String sort,
                                                  @RequestParam(required = false) String type,
                                                  @RequestParam(required = false) UUID guestId,
                                                  @RequestParam(required = false)
                                                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                  @RequestParam(required = false)
                                                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        Page<Transaction> result = transactionService.findAll(page, size, sort, type, guestId, from, to);
        return ApiResponse.ok(result.getContent(), PageResponse.from(result));
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('TRANSACTIONS:READ')")
    public ApiResponse<Map<String, BigDecimal>> summary(
            @RequestParam TransactionType type,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ApiResponse.ok(Map.of("total", transactionService.sumByTypeBetween(type, from, to)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('TRANSACTIONS:READ')")
    public ApiResponse<Transaction> findById(@PathVariable UUID id) {
        return ApiResponse.ok(transactionService.findById(id));
    }
}
