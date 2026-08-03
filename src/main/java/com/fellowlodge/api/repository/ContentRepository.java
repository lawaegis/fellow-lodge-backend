package com.fellowlodge.api.repository;

import com.fellowlodge.api.entity.AuditableEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

/**
 * Shared repository contract for admin content modules. Combines paging/sorting
 * with the Specification executor used by {@code ContentCrudService}.
 */
public interface ContentRepository<T extends AuditableEntity> extends JpaRepository<T, UUID>, JpaSpecificationExecutor<T> {
}
