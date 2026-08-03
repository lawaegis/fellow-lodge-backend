package com.fellowlodge.api.service;

import com.fellowlodge.api.common.exception.ResourceNotFoundException;
import com.fellowlodge.api.entity.AuditableEntity;
import com.fellowlodge.api.entity.Activatible;
import com.fellowlodge.api.repository.ContentRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

/**
 * Shared CRUD service for the admin content modules (menu, packages, banners,
 * announcements, policies, FAQs). Concrete services only supply the repository,
 * a resource label and a default sort column. List endpoints support the
 * standard {@code page}/{@code size}/{@code sort} contract plus an optional
 * {@code active} filter; every change is immediately visible to the public
 * catalog endpoints (single source of truth = the database).
 */
public abstract class ContentCrudService<T extends AuditableEntity & Activatible> {

    private final ContentRepository<T> repository;
    private final String resourceName;

    protected ContentCrudService(ContentRepository<T> repository, String resourceName) {
        this.repository = repository;
        this.resourceName = resourceName;
    }

    protected Sort defaultSort() {
        return Sort.by(Sort.Direction.ASC, "createdAt");
    }

    public Page<T> findAll(int page, int size, String sort, Boolean active) {
        Pageable pageable = PageRequest.of(page, size, hasText(sort) ? buildSort(sort) : defaultSort());
        Specification<T> spec = Specification.where(null);
        if (active != null) {
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("active"), active));
        }
        return repository.findAll(spec, pageable);
    }

    public List<T> findAll() {
        return repository.findAll(defaultSort());
    }

    public List<T> findActive() {
        return repository.findAll((root, cq, cb) -> cb.equal(root.get("active"), true), defaultSort());
    }

    public T findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(resourceName, id));
    }

    @Transactional
    public T create(T entity) {
        return repository.save(entity);
    }

    @Transactional
    public T update(UUID id, T updated) {
        T existing = findById(id);
        BeanUtils.copyProperties(updated, existing, "id", "createdAt", "updatedAt");
        return repository.save(existing);
    }

    @Transactional
    public void delete(UUID id) {
        repository.delete(findById(id));
    }

    @Transactional
    public T setActive(UUID id, boolean active) {
        T entity = findById(id);
        entity.setActive(active);
        return repository.save(entity);
    }

    protected Sort buildSort(String sort) {
        String[] parts = sort.split(",");
        String column = parts[0];
        Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1])
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, column);
    }

    private boolean hasText(String value) {
        return StringUtils.hasText(value);
    }
}
