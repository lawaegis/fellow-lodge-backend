package com.fellowlodge.api.service;

import com.fellowlodge.api.entity.EventPackage;
import com.fellowlodge.api.repository.EventPackageRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class EventPackageService extends ContentCrudService<EventPackage> {

    public EventPackageService(EventPackageRepository repository) {
        super(repository, "Event package");
    }

    @Override
    protected Sort defaultSort() {
        return Sort.by(Sort.Direction.ASC, "name");
    }
}
