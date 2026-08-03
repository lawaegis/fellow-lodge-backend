package com.fellowlodge.api.service;

import com.fellowlodge.api.common.exception.DuplicateResourceException;
import com.fellowlodge.api.common.exception.InvalidOperationException;
import com.fellowlodge.api.common.exception.ResourceNotFoundException;
import com.fellowlodge.api.entity.Amenity;
import com.fellowlodge.api.entity.RoomType;
import com.fellowlodge.api.repository.AmenityRepository;
import com.fellowlodge.api.repository.RoomRepository;
import com.fellowlodge.api.repository.RoomTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoomTypeService {

    private final RoomTypeRepository roomTypeRepository;
    private final RoomRepository roomRepository;
    private final AmenityRepository amenityRepository;

    public Page<RoomType> findAll(int page, int size, String sort, Boolean active) {
        Pageable pageable = PageRequest.of(page, size, buildSort(sort));
        Specification<RoomType> spec = Specification.where(null);
        if (active != null) {
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("active"), active));
        }
        return roomTypeRepository.findAll(spec, pageable);
    }

    public List<RoomType> findAll() {
        return roomTypeRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    public List<RoomType> findActive() {
        return roomTypeRepository.findByActiveTrue();
    }

    public RoomType findById(UUID id) {
        return roomTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room type", id));
    }

    public List<RoomType> findByIdIn(java.util.Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return roomTypeRepository.findByIdIn(ids);
    }

    @Transactional
    public RoomType create(RoomType roomType) {
        if (roomTypeRepository.existsByNameIgnoreCase(roomType.getName())) {
            throw new DuplicateResourceException("A room type already exists with name: " + roomType.getName());
        }
        return roomTypeRepository.save(roomType);
    }

    @Transactional
    public RoomType update(UUID id, RoomType updated) {
        RoomType roomType = findById(id);
        if (updated.getName() != null && !updated.getName().equalsIgnoreCase(roomType.getName())
                && roomTypeRepository.existsByNameIgnoreCase(updated.getName())) {
            throw new DuplicateResourceException("A room type already exists with name: " + updated.getName());
        }
        updated.setId(id);
        updated.setAmenitySet(roomType.getAmenitySet());
        return roomTypeRepository.save(updated);
    }

    @Transactional
    public void delete(UUID id) {
        RoomType roomType = findById(id);
        if (!roomRepository.findByRoomTypeId(id).isEmpty()) {
            throw new InvalidOperationException("Room type is assigned to rooms and cannot be deleted.");
        }
        roomTypeRepository.delete(roomType);
    }

    @Transactional
    public RoomType addAmenity(UUID roomTypeId, UUID amenityId) {
        RoomType roomType = findById(roomTypeId);
        Amenity amenity = amenityRepository.findById(amenityId)
                .orElseThrow(() -> new ResourceNotFoundException("Amenity", amenityId));
        roomType.getAmenitySet().add(amenity);
        return roomTypeRepository.save(roomType);
    }

    @Transactional
    public RoomType removeAmenity(UUID roomTypeId, UUID amenityId) {
        RoomType roomType = findById(roomTypeId);
        roomType.getAmenitySet().removeIf(amenity -> amenity.getId().equals(amenityId));
        return roomTypeRepository.save(roomType);
    }

    private Sort buildSort(String sort) {
        if (!StringUtils.hasText(sort)) {
            return Sort.by(Sort.Direction.ASC, "name");
        }
        String[] parts = sort.split(",");
        Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1])
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, parts[0]);
    }
}
