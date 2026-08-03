package com.fellowlodge.api.service;

import com.fellowlodge.api.common.exception.InvalidOperationException;
import com.fellowlodge.api.common.exception.ResourceNotFoundException;
import com.fellowlodge.api.dto.portal.AvailabilityResponse;
import com.fellowlodge.api.entity.ConferenceHall;
import com.fellowlodge.api.entity.EventBooking;
import com.fellowlodge.api.enums.EventBookingStatus;
import com.fellowlodge.api.repository.ConferenceHallRepository;
import com.fellowlodge.api.repository.EventBookingRepository;
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
public class ConferenceHallService {

    private final ConferenceHallRepository conferenceHallRepository;
    private final EventBookingRepository eventBookingRepository;

    public Page<ConferenceHall> findAll(int page, int size, String sort, Boolean active) {
        Pageable pageable = PageRequest.of(page, size, buildSort(sort));
        Specification<ConferenceHall> spec = Specification.where(null);
        if (active != null) {
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("active"), active));
        }
        return conferenceHallRepository.findAll(spec, pageable);
    }

    public List<ConferenceHall> findAll() {
        return conferenceHallRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    public List<ConferenceHall> findActive() {
        return conferenceHallRepository.findByActiveTrue();
    }

    public ConferenceHall findById(UUID id) {
        return conferenceHallRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conference hall", id));
    }

    public AvailabilityResponse availability(UUID hallId) {
        ConferenceHall hall = findById(hallId);
        int booked = eventBookingRepository.findByConferenceHallId(hallId).stream()
                .filter(booking -> booking.getStatus() != EventBookingStatus.Cancelled)
                .mapToInt(EventBooking::getNumberOfAttendees)
                .sum();
        int capacity = Math.max(0, hall.getCapacity());
        int remaining = Math.max(0, capacity - booked);
        return new AvailabilityResponse(remaining > 0, capacity, booked, remaining, hall.getId(), hall.getName());
    }

    @Transactional
    public ConferenceHall create(ConferenceHall hall) {
        return conferenceHallRepository.save(hall);
    }

    @Transactional
    public ConferenceHall update(UUID id, ConferenceHall updated) {
        updated.setId(id);
        return conferenceHallRepository.save(updated);
    }

    @Transactional
    public void delete(UUID id) {
        conferenceHallRepository.delete(findById(id));
    }

    @Transactional
    public ConferenceHall setActive(UUID id, boolean active) {
        ConferenceHall hall = findById(id);
        hall.setActive(active);
        return conferenceHallRepository.save(hall);
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
