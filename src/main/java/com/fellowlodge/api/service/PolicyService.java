package com.fellowlodge.api.service;

import com.fellowlodge.api.entity.Policy;
import com.fellowlodge.api.repository.PolicyRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class PolicyService extends ContentCrudService<Policy> {

    public PolicyService(PolicyRepository repository) {
        super(repository, "Policy");
    }

    @Override
    protected Sort defaultSort() {
        return Sort.by(Sort.Direction.ASC, "sortOrder");
    }
}
