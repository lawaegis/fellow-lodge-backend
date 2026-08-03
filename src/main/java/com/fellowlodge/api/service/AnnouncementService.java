package com.fellowlodge.api.service;

import com.fellowlodge.api.entity.Announcement;
import com.fellowlodge.api.repository.AnnouncementRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AnnouncementService extends ContentCrudService<Announcement> {

    private final AnnouncementRepository announcementRepository;

    public AnnouncementService(AnnouncementRepository repository) {
        super(repository, "Announcement");
        this.announcementRepository = repository;
    }

    public Announcement create(Announcement announcement, UUID createdBy) {
        announcement.setCreatedBy(createdBy);
        return super.create(announcement);
    }

    @Override
    protected Sort defaultSort() {
        return Sort.by(Sort.Direction.DESC, "createdAt");
    }
}
