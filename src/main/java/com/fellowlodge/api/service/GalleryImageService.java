package com.fellowlodge.api.service;

import com.fellowlodge.api.common.exception.ResourceNotFoundException;
import com.fellowlodge.api.entity.GalleryImage;
import com.fellowlodge.api.repository.GalleryImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GalleryImageService {

    private final GalleryImageRepository galleryImageRepository;

    public List<GalleryImage> findAll() {
        return galleryImageRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public List<GalleryImage> findActive() {
        return galleryImageRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .filter(GalleryImage::isActive)
                .toList();
    }

    public GalleryImage findById(UUID id) {
        return galleryImageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gallery image", id));
    }

    @Transactional
    public GalleryImage create(GalleryImage image) {
        return galleryImageRepository.save(image);
    }

    @Transactional
    public GalleryImage update(UUID id, GalleryImage updated) {
        GalleryImage image = findById(id);
        updated.setId(id);
        return galleryImageRepository.save(updated);
    }

    @Transactional
    public void delete(UUID id) {
        galleryImageRepository.delete(findById(id));
    }

    @Transactional
    public GalleryImage setActive(UUID id, boolean active) {
        GalleryImage image = findById(id);
        image.setActive(active);
        return galleryImageRepository.save(image);
    }
}
