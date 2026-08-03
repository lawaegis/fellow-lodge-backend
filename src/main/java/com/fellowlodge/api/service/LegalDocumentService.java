package com.fellowlodge.api.service;

import com.fellowlodge.api.common.exception.BusinessException;
import com.fellowlodge.api.entity.LegalDocument;
import com.fellowlodge.api.repository.LegalDocumentRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Admin CRUD for legal documents (privacy policy, terms, etc.) served by the
 * guest portal at /public/hotel/legal/{slug}.
 */
@Service
public class LegalDocumentService extends ContentCrudService<LegalDocument> {

    private final LegalDocumentRepository repository;

    public LegalDocumentService(LegalDocumentRepository repository) {
        super(repository, "Legal document");
        this.repository = repository;
    }

    @Override
    protected Sort defaultSort() {
        return Sort.by(Sort.Direction.ASC, "title");
    }

    @Override
    @Transactional
    public LegalDocument create(LegalDocument entity) {
        if (repository.findBySlug(entity.getSlug()).isPresent()) {
            throw new BusinessException("A legal document with slug '" + entity.getSlug() + "' already exists.",
                    HttpStatus.CONFLICT);
        }
        return super.create(entity);
    }

    @Override
    @Transactional
    public LegalDocument update(java.util.UUID id, LegalDocument updated) {
        if (updated.getSlug() != null) {
            repository.findBySlug(updated.getSlug())
                    .filter(existing -> !existing.getId().equals(id))
                    .ifPresent(existing -> {
                        throw new BusinessException("A legal document with slug '" + updated.getSlug()
                                + "' already exists.", HttpStatus.CONFLICT);
                    });
        }
        return super.update(id, updated);
    }
}
