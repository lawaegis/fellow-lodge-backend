package com.fellowlodge.api.service;

import com.fellowlodge.api.common.exception.DuplicateResourceException;
import com.fellowlodge.api.common.exception.ResourceNotFoundException;
import com.fellowlodge.api.entity.Amenity;
import com.fellowlodge.api.repository.AmenityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AmenityService {

    private final AmenityRepository amenityRepository;

    public List<Amenity> findAll() {
        return amenityRepository.findAll();
    }

    public Amenity findById(UUID id) {
        return amenityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Amenity", id));
    }

    @Transactional
    public Amenity create(Amenity amenity) {
        if (amenityRepository.findAll().stream()
                .anyMatch(existing -> existing.getName().equalsIgnoreCase(amenity.getName()))) {
            throw new DuplicateResourceException("An amenity already exists with name: " + amenity.getName());
        }
        return amenityRepository.save(amenity);
    }

    @Transactional
    public Amenity update(UUID id, Amenity updated) {
        Amenity amenity = findById(id);
        if (amenityRepository.findAll().stream()
                .anyMatch(existing -> !existing.getId().equals(id)
                        && existing.getName().equalsIgnoreCase(updated.getName()))) {
            throw new DuplicateResourceException("An amenity already exists with name: " + updated.getName());
        }
        updated.setId(id);
        return amenityRepository.save(updated);
    }

    @Transactional
    public void delete(UUID id) {
        amenityRepository.delete(findById(id));
    }
}
