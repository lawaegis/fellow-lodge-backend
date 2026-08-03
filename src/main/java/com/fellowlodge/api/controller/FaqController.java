package com.fellowlodge.api.controller;

import com.fellowlodge.api.common.ApiResponse;
import com.fellowlodge.api.common.PageResponse;
import com.fellowlodge.api.entity.Faq;
import com.fellowlodge.api.service.FaqService;
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

@RestController
@RequestMapping("/api/faqs")
@RequiredArgsConstructor
public class FaqController {

    private final FaqService faqService;

    @GetMapping
    @PreAuthorize("hasAuthority('FAQS:READ')")
    public ApiResponse<List<Faq>> findAll(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "20") int size,
                                          @RequestParam(required = false) String sort,
                                          @RequestParam(required = false) Boolean active) {
        Page<Faq> result = faqService.findAll(page, size, sort, active);
        return ApiResponse.ok(result.getContent(), PageResponse.from(result));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('FAQS:READ')")
    public ApiResponse<List<Faq>> findAll() {
        return ApiResponse.ok(faqService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('FAQS:READ')")
    public ApiResponse<Faq> findById(@PathVariable UUID id) {
        return ApiResponse.ok(faqService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('FAQS:WRITE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Faq> create(@Valid @RequestBody Faq faq) {
        return ApiResponse.created("FAQ created", faqService.create(faq));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('FAQS:WRITE')")
    public ApiResponse<Faq> update(@PathVariable UUID id, @Valid @RequestBody Faq faq) {
        return ApiResponse.ok("FAQ updated", faqService.update(id, faq));
    }

    @PostMapping("/{id}/active")
    @PreAuthorize("hasAuthority('FAQS:WRITE')")
    public ApiResponse<Faq> setActive(@PathVariable UUID id, @RequestParam boolean active) {
        return ApiResponse.ok("FAQ updated", faqService.setActive(id, active));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('FAQS:DELETE')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        faqService.delete(id);
        return ApiResponse.deleted("FAQ deleted");
    }
}
