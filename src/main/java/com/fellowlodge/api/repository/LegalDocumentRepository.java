package com.fellowlodge.api.repository;

import com.fellowlodge.api.entity.LegalDocument;

import java.util.Optional;

public interface LegalDocumentRepository extends ContentRepository<LegalDocument> {

    Optional<LegalDocument> findBySlug(String slug);

    Optional<LegalDocument> findBySlugAndActiveTrue(String slug);
}
