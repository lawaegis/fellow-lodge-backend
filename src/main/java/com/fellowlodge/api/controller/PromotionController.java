package com.fellowlodge.api.controller;

import com.fellowlodge.api.common.ApiResponse;
import com.fellowlodge.api.common.PageResponse;
import com.fellowlodge.api.dto.portal.PromoValidateRequest;
import com.fellowlodge.api.dto.portal.PromoValidateResponse;
import com.fellowlodge.api.entity.Promotion;
import com.fellowlodge.api.service.PromotionService;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    @GetMapping
    @PreAuthorize("hasAuthority('PROMOTIONS:READ')")
    public ApiResponse<List<Promotion>> findAll(@RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "20") int size,
                                                @RequestParam(required = false) String sort,
                                                @RequestParam(required = false) Boolean active) {
        Page<Promotion> result = promotionService.findAll(page, size, sort, active);
        return ApiResponse.ok(result.getContent(), PageResponse.from(result));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('PROMOTIONS:READ')")
    public ApiResponse<List<Promotion>> findAll() {
        return ApiResponse.ok(promotionService.findAll());
    }

    @PostMapping("/validate")
    public ApiResponse<PromoValidateResponse> validate(@Valid @RequestBody PromoValidateRequest request) {
        try {
            Promotion promotion = promotionService.validate(request.code());
            BigDecimal discountPercent = promotion.getDiscountPercent() == null
                    ? BigDecimal.ZERO : promotion.getDiscountPercent();
            BigDecimal discountAmount = promotion.getDiscountAmount() == null
                    ? BigDecimal.ZERO : promotion.getDiscountAmount();
            if (discountAmount.signum() == 0 && discountPercent.signum() > 0 && request.subtotal() != null) {
                discountAmount = discountPercent
                        .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
                        .multiply(request.subtotal())
                        .setScale(2, RoundingMode.HALF_UP);
            }
            return ApiResponse.ok(new PromoValidateResponse(true, promotion.getCode(),
                    discountPercent, discountAmount, "Promotion code is valid."));
        } catch (Exception ex) {
            return ApiResponse.ok(new PromoValidateResponse(false, request.code(),
                    BigDecimal.ZERO, BigDecimal.ZERO, ex.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PROMOTIONS:READ')")
    public ApiResponse<Promotion> findById(@PathVariable UUID id) {
        return ApiResponse.ok(promotionService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PROMOTIONS:WRITE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Promotion> create(@Valid @RequestBody Promotion promotion) {
        return ApiResponse.created("Promotion created", promotionService.create(promotion));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PROMOTIONS:WRITE')")
    public ApiResponse<Promotion> update(@PathVariable UUID id, @Valid @RequestBody Promotion promotion) {
        return ApiResponse.ok("Promotion updated", promotionService.update(id, promotion));
    }

    @PostMapping("/{id}/active")
    @PreAuthorize("hasAuthority('PROMOTIONS:WRITE')")
    public ApiResponse<Promotion> setActive(@PathVariable UUID id, @RequestParam boolean active) {
        return ApiResponse.ok("Promotion updated", promotionService.setActive(id, active));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PROMOTIONS:DELETE')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        promotionService.delete(id);
        return ApiResponse.deleted("Promotion deleted");
    }
}
