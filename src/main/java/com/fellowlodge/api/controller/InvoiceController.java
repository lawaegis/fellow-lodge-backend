package com.fellowlodge.api.controller;

import com.fellowlodge.api.common.ApiResponse;
import com.fellowlodge.api.common.PageResponse;
import com.fellowlodge.api.entity.Invoice;
import com.fellowlodge.api.security.SecurityUtils;
import com.fellowlodge.api.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping
    @PreAuthorize("hasAuthority('INVOICES:READ')")
    public ApiResponse<List<Invoice>> findAll(@RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "20") int size,
                                              @RequestParam(required = false) String sort,
                                              @RequestParam(required = false) String search,
                                              @RequestParam(required = false) String status,
                                              @RequestParam(required = false) UUID reservationId,
                                              @RequestParam(required = false) UUID guestId) {
        Page<Invoice> result = invoiceService.findAll(page, size, sort, search, status, reservationId, guestId);
        return ApiResponse.ok(result.getContent(), PageResponse.from(result));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('INVOICES:READ')")
    public ApiResponse<List<Invoice>> findAll() {
        return ApiResponse.ok(invoiceService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('INVOICES:READ')")
    public ApiResponse<Invoice> findById(@PathVariable UUID id) {
        return ApiResponse.ok(invoiceService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('INVOICES:WRITE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Invoice> create(@Valid @RequestBody Invoice invoice) {
        return ApiResponse.created("Invoice created",
                invoiceService.create(invoice, SecurityUtils.currentUserId()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('INVOICES:WRITE')")
    public ApiResponse<Invoice> update(@PathVariable UUID id, @Valid @RequestBody Invoice invoice) {
        return ApiResponse.ok("Invoice updated", invoiceService.update(id, invoice));
    }

    @PostMapping("/{id}/mark-paid")
    @PreAuthorize("hasAuthority('INVOICES:WRITE')")
    public ApiResponse<Invoice> markPaid(@PathVariable UUID id) {
        return ApiResponse.ok("Invoice marked as paid", invoiceService.markPaid(id));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('INVOICES:WRITE')")
    public ApiResponse<Invoice> cancel(@PathVariable UUID id) {
        return ApiResponse.ok("Invoice cancelled", invoiceService.markCancelled(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('INVOICES:DELETE')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        invoiceService.delete(id);
        return ApiResponse.deleted("Invoice deleted");
    }
}
