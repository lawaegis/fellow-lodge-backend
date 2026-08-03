package com.fellowlodge.api.controller;

import com.fellowlodge.api.common.ApiResponse;
import com.fellowlodge.api.common.PageResponse;
import com.fellowlodge.api.entity.LegalDocument;
import com.fellowlodge.api.service.LegalDocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Admin CRUD for legal documents served by the guest portal at
 * /public/hotel/legal/{slug} (privacy policy, terms of stay, etc.).
 */
@RestController
@RequestMapping("/api/legal-docs")
@RequiredArgsConstructor
public class LegalDocumentController {

    private final LegalDocumentService legalDocumentService;

    @GetMapping
    @PreAuthorize("hasAuthority('LEGAL_DOCS:READ')")
    public ApiResponse<List<LegalDocument>> findAll(@RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "20") int size,
                                                    @RequestParam(required = false) String sort,
                                                    @RequestParam(required = false) Boolean active) {
        Page<LegalDocument> result = legalDocumentService.findAll(page, size, sort, active);
        return ApiResponse.ok(result.getContent(), PageResponse.from(result));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('LEGAL_DOCS:READ')")
    public ApiResponse<List<LegalDocument>> findAll() {
        return ApiResponse.ok(legalDocumentService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('LEGAL_DOCS:READ')")
    public ApiResponse<LegalDocument> findById(@PathVariable UUID id) {
        return ApiResponse.ok(legalDocumentService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('LEGAL_DOCS:WRITE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<LegalDocument> create(@Valid @RequestBody LegalDocument document) {
        return ApiResponse.created("Legal document created", legalDocumentService.create(document));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('LEGAL_DOCS:WRITE')")
    public ApiResponse<LegalDocument> update(@PathVariable UUID id, @Valid @RequestBody LegalDocument document) {
        return ApiResponse.ok("Legal document updated", legalDocumentService.update(id, document));
    }

    @PostMapping("/{id}/active")
    @PreAuthorize("hasAuthority('LEGAL_DOCS:WRITE')")
    public ApiResponse<LegalDocument> setActive(@PathVariable UUID id, @RequestParam boolean active) {
        return ApiResponse.ok("Legal document updated", legalDocumentService.setActive(id, active));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('LEGAL_DOCS:DELETE')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        legalDocumentService.delete(id);
        return ApiResponse.deleted("Legal document deleted");
    }
}
