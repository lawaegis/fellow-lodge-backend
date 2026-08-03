package com.fellowlodge.api.service;

import com.fellowlodge.api.entity.Faq;
import com.fellowlodge.api.repository.FaqRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class FaqService extends ContentCrudService<Faq> {

    public FaqService(FaqRepository repository) {
        super(repository, "FAQ");
    }

    @Override
    protected Sort defaultSort() {
        return Sort.by(Sort.Direction.ASC, "sortOrder");
    }
}
