package com.fellowlodge.api.service;

import com.fellowlodge.api.entity.ConferencePackage;
import com.fellowlodge.api.repository.ConferencePackageRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class ConferencePackageService extends ContentCrudService<ConferencePackage> {

    public ConferencePackageService(ConferencePackageRepository repository) {
        super(repository, "Conference package");
    }

    @Override
    protected Sort defaultSort() {
        return Sort.by(Sort.Direction.ASC, "name");
    }
}
