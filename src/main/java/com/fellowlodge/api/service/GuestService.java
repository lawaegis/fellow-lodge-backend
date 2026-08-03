package com.fellowlodge.api.service;

import com.fellowlodge.api.common.exception.DuplicateResourceException;
import com.fellowlodge.api.common.exception.InvalidOperationException;
import com.fellowlodge.api.common.exception.ResourceNotFoundException;
import com.fellowlodge.api.entity.Guest;
import com.fellowlodge.api.repository.GuestRepository;
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
public class GuestService {

    private final GuestRepository guestRepository;

    public Page<Guest> findAll(int page, int size, String sort, String search, Boolean vip) {
        Pageable pageable = PageRequest.of(page, size, buildSort(sort));
        Specification<Guest> spec = Specification.where(null);
        if (StringUtils.hasText(search)) {
            String q = search.toLowerCase();
            spec = spec.and((root, cq, cb) -> cb.or(
                    cb.like(cb.lower(cb.concat(cb.concat(root.get("firstName"), " "), root.get("lastName"))),
                            "%" + q + "%"),
                    cb.like(cb.lower(cb.coalesce(root.get("email"), "")), "%" + q + "%"),
                    cb.like(cb.lower(cb.coalesce(root.get("phone"), "")), "%" + q + "%")));
        }
        if (vip != null) {
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("vip"), vip));
        }
        return guestRepository.findAll(spec, pageable);
    }

    public List<Guest> findAll() {
        return guestRepository.findAll(Sort.by(Sort.Direction.ASC, "firstName"));
    }

    public Guest findById(UUID id) {
        return guestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Guest", id));
    }

    public Guest findByEmail(String email) {
        return guestRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Guest with email " + email));
    }

    public Guest findByUserId(UUID userId) {
        return guestRepository.findByUserId(userId).orElse(null);
    }

    public List<Guest> search(String query) {
        return guestRepository.search(query);
    }

    public long count() {
        return guestRepository.count();
    }

    public long countVip() {
        return guestRepository.findByVipTrue().size();
    }

    @Transactional
    public Guest create(Guest guest) {
        if (guest.getEmail() != null && guestRepository.findByEmailIgnoreCase(guest.getEmail()).isPresent()) {
            throw new DuplicateResourceException("A guest already exists for email: " + guest.getEmail());
        }
        if (guest.getFirstName() == null || guest.getFirstName().isBlank()) {
            throw new InvalidOperationException("First name is required.");
        }
        return guestRepository.save(guest);
    }

    @Transactional
    public Guest update(UUID id, Guest updated) {
        Guest guest = findById(id);
        if (updated.getEmail() != null && !updated.getEmail().equalsIgnoreCase(guest.getEmail())
                && guestRepository.findByEmailIgnoreCase(updated.getEmail()).isPresent()) {
            throw new DuplicateResourceException("A guest already exists for email: " + updated.getEmail());
        }
        updated.setId(id);
        updated.setUserId(guest.getUserId());
        return guestRepository.save(updated);
    }

    @Transactional
    public void delete(UUID id) {
        guestRepository.delete(findById(id));
    }

    @Transactional
    public Guest setVip(UUID id, boolean vip) {
        Guest guest = findById(id);
        guest.setVip(vip);
        return guestRepository.save(guest);
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
