package com.fellowlodge.api.service;

import com.fellowlodge.api.entity.Banner;
import com.fellowlodge.api.repository.BannerRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class BannerService extends ContentCrudService<Banner> {

    public BannerService(BannerRepository repository) {
        super(repository, "Banner");
    }

    @Override
    protected Sort defaultSort() {
        return Sort.by(Sort.Direction.ASC, "title");
    }
}
