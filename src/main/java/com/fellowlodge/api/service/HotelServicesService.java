package com.fellowlodge.api.service;

import com.fellowlodge.api.common.exception.ResourceNotFoundException;
import com.fellowlodge.api.entity.HotelService;
import com.fellowlodge.api.repository.HotelServiceRepository;
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
public class HotelServicesService {

    private final HotelServiceRepository hotelServiceRepository;

    public Page<HotelService> findAll(int page, int size, String sort, String category, Boolean active) {
        Pageable pageable = PageRequest.of(page, size, buildSort(sort));
        Specification<HotelService> spec = Specification.where(null);
        if (StringUtils.hasText(category)) {
            spec = spec.and((root, cq, cb) -> cb.equal(cb.lower(root.get("category")), category.toLowerCase()));
        }
        if (active != null) {
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("active"), active));
        }
        return hotelServiceRepository.findAll(spec, pageable);
    }

    public List<HotelService> findAll() {
        return hotelServiceRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    public List<HotelService> findActive() {
        return hotelServiceRepository.findByActiveTrue();
    }

    public List<HotelService> findByCategory(String category) {
        return hotelServiceRepository.findByCategoryIgnoreCase(category);
    }

    public HotelService findById(UUID id) {
        return hotelServiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel service", id));
    }

    @Transactional
    public HotelService create(HotelService service) {
        return hotelServiceRepository.save(service);
    }

    @Transactional
    public HotelService update(UUID id, HotelService updated) {
        updated.setId(id);
        return hotelServiceRepository.save(updated);
    }

    @Transactional
    public void delete(UUID id) {
        hotelServiceRepository.delete(findById(id));
    }

    @Transactional
    public HotelService setActive(UUID id, boolean active) {
        HotelService service = findById(id);
        service.setActive(active);
        return hotelServiceRepository.save(service);
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
