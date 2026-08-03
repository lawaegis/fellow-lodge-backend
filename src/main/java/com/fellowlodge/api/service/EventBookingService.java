package com.fellowlodge.api.service;

import com.fellowlodge.api.common.exception.InvalidOperationException;
import com.fellowlodge.api.common.exception.ResourceNotFoundException;
import com.fellowlodge.api.entity.Event;
import com.fellowlodge.api.entity.EventBooking;
import com.fellowlodge.api.enums.EventBookingStatus;
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
public class EventBookingService {

    private final EventBookingRepository eventBookingRepository;
    private final EventRepository eventRepository;

    public Page<EventBooking> findAll(int page, int size, String sort, String status, UUID guestId, UUID eventId) {
        Pageable pageable = PageRequest.of(page, size, buildSort(sort));
        Specification<EventBooking> spec = Specification.where(null);
        if (StringUtils.hasText(status)) {
            EventBookingStatus bs = EventBookingStatus.valueOf(status);
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("status"), bs));
        }
        if (guestId != null) {
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("guestId"), guestId));
        }
        if (eventId != null) {
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("eventId"), eventId));
        }
        return eventBookingRepository.findAll(spec, pageable);
    }

    public List<EventBooking> findByGuestId(UUID guestId) {
        return eventBookingRepository.findByGuestId(guestId);
    }

    public EventBooking findById(UUID id) {
        return eventBookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event booking", id));
    }

    @Transactional
    public EventBooking create(EventBooking booking, UUID bookedBy) {
        booking.setBookedBy(bookedBy);
        booking.setBookingDate(LocalDateTime.now());
        if (booking.getStatus() == null) {
            booking.setStatus(EventBookingStatus.Pending);
        }
        if (booking.getEventId() != null && (booking.getAmount() == null || booking.getAmount().signum() == 0)) {
            eventRepository.findById(booking.getEventId()).ifPresent(event ->
                    booking.setAmount(event.getPrice().multiply(java.math.BigDecimal.valueOf(
                            Math.max(1, booking.getNumberOfAttendees())))));
        }
        return eventBookingRepository.save(booking);
    }

    @Transactional
    public EventBooking update(UUID id, EventBooking updated) {
        EventBooking booking = findById(id);
        updated.setId(id);
        updated.setBookedBy(booking.getBookedBy());
        updated.setBookingDate(booking.getBookingDate());
        updated.setStatus(booking.getStatus());
        return eventBookingRepository.save(updated);
    }

    @Transactional
    public EventBooking confirm(UUID id) {
        EventBooking booking = findById(id);
        if (booking.getStatus() != EventBookingStatus.Pending) {
            throw new InvalidOperationException("Only pending bookings can be confirmed.");
        }
        booking.setStatus(EventBookingStatus.Confirmed);
        return eventBookingRepository.save(booking);
    }

    @Transactional
    public EventBooking cancel(UUID id, String reason) {
        EventBooking booking = findById(id);
        if (booking.getStatus() == EventBookingStatus.Completed) {
            throw new InvalidOperationException("Completed bookings cannot be cancelled.");
        }
        booking.setStatus(EventBookingStatus.Cancelled);
        booking.setNotes(booking.getNotes() == null ? reason : booking.getNotes() + " [Cancelled: " + reason + "]");
        return eventBookingRepository.save(booking);
    }

    @Transactional
    public EventBooking complete(UUID id) {
        EventBooking booking = findById(id);
        booking.setStatus(EventBookingStatus.Completed);
        return eventBookingRepository.save(booking);
    }

    private Sort buildSort(String sort) {
        if (!StringUtils.hasText(sort)) {
            return Sort.by(Sort.Direction.DESC, "bookingDate");
        }
        String[] parts = sort.split(",");
        Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1])
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, parts[0]);
    }
}
