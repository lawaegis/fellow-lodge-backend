package com.fellowlodge.api.service;

import com.fellowlodge.api.entity.Attraction;
import com.fellowlodge.api.repository.AttractionRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 * Admin CRUD for local attractions shown on the guest portal
 * (/public/hotel/attractions). Backed by {@link ContentCrudService} so every
 * change is immediately visible to the portal (single source of truth).
 */
@Service
public class AttractionService extends ContentCrudService<Attraction> {

    public AttractionService(AttractionRepository repository) {
        super(repository, "Attraction");
    }

    @Override
    protected Sort defaultSort() {
        return Sort.by(Sort.Direction.ASC, "sortOrder");
    }
}
