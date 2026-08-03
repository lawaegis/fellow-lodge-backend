package com.fellowlodge.api.service;

import com.fellowlodge.api.common.exception.ResourceNotFoundException;
import com.fellowlodge.api.entity.Staff;
import com.fellowlodge.api.repository.StaffRepository;
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
public class StaffService {

    private final StaffRepository staffRepository;

    public Page<Staff> findAll(int page, int size, String sort, String search, String department) {
        Pageable pageable = PageRequest.of(page, size, buildSort(sort));
        Specification<Staff> spec = Specification.where(null);
        if (StringUtils.hasText(search)) {
            String q = search.toLowerCase();
            spec = spec.and((root, cq, cb) -> cb.or(
                    cb.like(cb.lower(cb.concat(cb.concat(root.get("firstName"), " "), root.get("lastName"))),
                            "%" + q + "%"),
                    cb.like(cb.lower(cb.coalesce(root.get("email"), "")), "%" + q + "%"),
                    cb.like(cb.lower(cb.coalesce(root.get("position"), "")), "%" + q + "%")));
        }
        if (StringUtils.hasText(department)) {
            spec = spec.and((root, cq, cb) -> cb.equal(cb.lower(root.get("department")), department.toLowerCase()));
        }
        return staffRepository.findAll(spec, pageable);
    }

    public List<Staff> findAll() {
        return staffRepository.findAll(Sort.by(Sort.Direction.ASC, "firstName"));
    }

    public Staff findById(UUID id) {
        return staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff", id));
    }

    public Staff findByUserId(UUID userId) {
        return staffRepository.findByUserId(userId).orElse(null);
    }

    public List<Staff> search(String query) {
        return staffRepository.search(query);
    }

    @Transactional
    public Staff create(Staff staff) {
        return staffRepository.save(staff);
    }

    @Transactional
    public Staff update(UUID id, Staff updated) {
        Staff staff = findById(id);
        updated.setId(id);
        updated.setUserId(staff.getUserId());
        return staffRepository.save(updated);
    }

    @Transactional
    public void delete(UUID id) {
        staffRepository.delete(findById(id));
    }

    private Sort buildSort(String sort) {
        if (!StringUtils.hasText(sort)) {
            return Sort.by(Sort.Direction.ASC, "firstName");
        }
        String[] parts = sort.split(",");
        Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1])
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, parts[0]);
    }
}
