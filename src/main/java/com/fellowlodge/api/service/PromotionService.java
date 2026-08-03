package com.fellowlodge.api.service;

import com.fellowlodge.api.common.exception.DuplicateResourceException;
import com.fellowlodge.api.common.exception.InvalidOperationException;
import com.fellowlodge.api.common.exception.ResourceNotFoundException;
import com.fellowlodge.api.entity.Promotion;
import com.fellowlodge.api.repository.PromotionRepository;
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
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PromotionService {

    private final PromotionRepository promotionRepository;

    public Page<Promotion> findAll(int page, int size, String sort, Boolean active) {
        Pageable pageable = PageRequest.of(page, size, buildSort(sort));
        Specification<Promotion> spec = Specification.where(null);
        if (active != null) {
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("active"), active));
        }
        return promotionRepository.findAll(spec, pageable);
    }

    public List<Promotion> findAll() {
        return promotionRepository.findAll(Sort.by(Sort.Direction.ASC, "validFrom"));
    }

    public List<Promotion> findActive() {
        return promotionRepository.findByActiveTrueAndValidToGreaterThanEqual(LocalDate.now());
    }

    public Promotion findById(UUID id) {
        return promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", id));
    }

    public Promotion findByCode(String code) {
        return promotionRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion with code " + code));
    }

    public Promotion validate(String code) {
        Promotion promotion = promotionRepository.findByCodeIgnoreCase(code)
                .filter(Promotion::isActive)
                .filter(p -> p.getValidFrom() == null || !p.getValidFrom().isAfter(LocalDate.now()))
                .filter(p -> p.getValidTo() == null || !p.getValidTo().isBefore(LocalDate.now()))
                .orElseThrow(() -> new InvalidOperationException("Promotion code is invalid or expired."));
        return promotion;
    }

    @Transactional
    public Promotion create(Promotion promotion) {
        if (promotionRepository.findByCodeIgnoreCase(promotion.getCode()).isPresent()) {
            throw new DuplicateResourceException("A promotion already exists with code: " + promotion.getCode());
        }
        return promotionRepository.save(promotion);
    }

    @Transactional
    public Promotion update(UUID id, Promotion updated) {
        Promotion promotion = findById(id);
        if (updated.getCode() != null && !updated.getCode().equalsIgnoreCase(promotion.getCode())
                && promotionRepository.findByCodeIgnoreCase(updated.getCode()).isPresent()) {
            throw new DuplicateResourceException("A promotion already exists with code: " + updated.getCode());
        }
        updated.setId(id);
        return promotionRepository.save(updated);
    }

    @Transactional
    public void delete(UUID id) {
        promotionRepository.delete(findById(id));
    }

    @Transactional
    public Promotion setActive(UUID id, boolean active) {
        Promotion promotion = findById(id);
        promotion.setActive(active);
        return promotionRepository.save(promotion);
    }

    private Sort buildSort(String sort) {
        if (!StringUtils.hasText(sort)) {
            return Sort.by(Sort.Direction.ASC, "validFrom");
        }
        String[] parts = sort.split(",");
        Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1])
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, parts[0]);
    }
}
