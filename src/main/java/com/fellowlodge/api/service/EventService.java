package com.fellowlodge.api.service;

import com.fellowlodge.api.common.exception.InvalidOperationException;
import com.fellowlodge.api.common.exception.ResourceNotFoundException;
import com.fellowlodge.api.dto.portal.AvailabilityResponse;
import com.fellowlodge.api.entity.Event;
import com.fellowlodge.api.entity.EventBooking;
import com.fellowlodge.api.enums.EventBookingStatus;
import com.fellowlodge.api.enums.EventStatus;
import com.fellowlodge.api.repository.EventBookingRepository;
import com.fellowlodge.api.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventBookingRepository eventBookingRepository;

    public Page<Event> findAll(int page, int size, String sort, String status) {
        Pageable pageable = PageRequest.of(page, size, buildSort(sort));
        Specification<Event> spec = Specification.where(null);
        if (StringUtils.hasText(status)) {
            EventStatus es = EventStatus.valueOf(status);
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("status"), es));
        }
        return eventRepository.findAll(spec, pageable);
    }

    public List<Event> findAll() {
        return eventRepository.findAll(Sort.by(Sort.Direction.ASC, "eventDate"));
    }

    public List<Event> findUpcoming() {
        return eventRepository.findByEventDateAfterOrderByEventDateAsc(LocalDateTime.now());
    }

    public Event findById(UUID id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", id));
    }

    public AvailabilityResponse availability(UUID eventId) {
        Event event = findById(eventId);
        int booked = eventBookingRepository.findByEventId(eventId).stream()
                .filter(booking -> booking.getStatus() != EventBookingStatus.Cancelled)
                .mapToInt(EventBooking::getNumberOfAttendees)
                .sum();
        int capacity = Math.max(0, event.getCapacity());
        int remaining = Math.max(0, capacity - booked);
        return new AvailabilityResponse(remaining > 0, capacity, booked, remaining, event.getId(), event.getName());
    }

    @Transactional
    public Event create(Event event, UUID createdBy) {
        event.setCreatedBy(createdBy);
        if (event.getStatus() == null) {
            event.setStatus(EventStatus.Upcoming);
        }
        return eventRepository.save(event);
    }

    @Transactional
    public Event update(UUID id, Event updated) {
        Event event = findById(id);
        updated.setId(id);
        updated.setCreatedBy(event.getCreatedBy());
        if (updated.getStatus() == null) {
            updated.setStatus(event.getStatus());
        }
        return eventRepository.save(updated);
    }

    @Transactional
    public void delete(UUID id) {
        eventRepository.delete(findById(id));
    }

    @Transactional
    public Event setStatus(UUID id, EventStatus status) {
        Event event = findById(id);
        event.setStatus(status);
        return eventRepository.save(event);
    }

    @Transactional
    public Event cancel(UUID id) {
        return setStatus(id, EventStatus.Cancelled);
    }

    private Sort buildSort(String sort) {
        if (!StringUtils.hasText(sort)) {
            return Sort.by(Sort.Direction.ASC, "eventDate");
        }
        String[] parts = sort.split(",");
        Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1])
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, parts[0]);
    }
}
