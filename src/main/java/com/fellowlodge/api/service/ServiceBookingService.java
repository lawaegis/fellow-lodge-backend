package com.fellowlodge.api.service;

import com.fellowlodge.api.common.exception.InvalidOperationException;
import com.fellowlodge.api.common.exception.ResourceNotFoundException;
import com.fellowlodge.api.entity.HotelService;
import com.fellowlodge.api.entity.ServiceBooking;
import com.fellowlodge.api.enums.ServiceBookingStatus;
import com.fellowlodge.api.repository.HotelServiceRepository;
import com.fellowlodge.api.repository.ServiceBookingRepository;
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
public class ServiceBookingService {

    private final ServiceBookingRepository serviceBookingRepository;
    private final HotelServiceRepository hotelServiceRepository;

    public Page<ServiceBooking> findAll(int page, int size, String sort, String status, UUID guestId) {
        Pageable pageable = PageRequest.of(page, size, buildSort(sort));
        Specification<ServiceBooking> spec = Specification.where(null);
        if (StringUtils.hasText(status)) {
            ServiceBookingStatus bs = ServiceBookingStatus.valueOf(status);
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("status"), bs));
        }
        if (guestId != null) {
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("guestId"), guestId));
        }
        return serviceBookingRepository.findAll(spec, pageable);
    }

    public List<ServiceBooking> findByGuestId(UUID guestId) {
        return serviceBookingRepository.findByGuestId(guestId);
    }

    public ServiceBooking findById(UUID id) {
        return serviceBookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service booking", id));
    }

    @Transactional
    public ServiceBooking create(ServiceBooking booking) {
        booking.setBookingDate(LocalDateTime.now());
        if (booking.getStatus() == null) {
            booking.setStatus(ServiceBookingStatus.Pending);
        }
        if (booking.getServiceId() != null) {
            HotelService service = hotelServiceRepository.findById(booking.getServiceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Hotel service", booking.getServiceId()));
            if (booking.getTotalAmount() == null || booking.getTotalAmount().signum() == 0) {
                booking.setTotalAmount(service.getPrice()
                        .multiply(java.math.BigDecimal.valueOf(Math.max(1, booking.getQuantity()))));
            }
        }
        return serviceBookingRepository.save(booking);
    }

    @Transactional
    public ServiceBooking update(UUID id, ServiceBooking updated) {
        ServiceBooking booking = findById(id);
        updated.setId(id);
        updated.setBookingDate(booking.getBookingDate());
        updated.setStatus(booking.getStatus());
        return serviceBookingRepository.save(updated);
    }

    @Transactional
    public ServiceBooking confirm(UUID id) {
        ServiceBooking booking = findById(id);
        if (booking.getStatus() != ServiceBookingStatus.Pending) {
            throw new InvalidOperationException("Only pending bookings can be confirmed.");
        }
        booking.setStatus(ServiceBookingStatus.Confirmed);
        return serviceBookingRepository.save(booking);
    }

    @Transactional
    public ServiceBooking complete(UUID id) {
        ServiceBooking booking = findById(id);
        if (booking.getStatus() == ServiceBookingStatus.Cancelled) {
            throw new InvalidOperationException("Cancelled bookings cannot be completed.");
        }
        booking.setStatus(ServiceBookingStatus.Completed);
        return serviceBookingRepository.save(booking);
    }

    @Transactional
    public ServiceBooking cancel(UUID id, String reason) {
        ServiceBooking booking = findById(id);
        if (booking.getStatus() == ServiceBookingStatus.Completed) {
            throw new InvalidOperationException("Completed bookings cannot be cancelled.");
        }
        booking.setStatus(ServiceBookingStatus.Cancelled);
        booking.setNotes(booking.getNotes() == null ? reason : booking.getNotes() + " [Cancelled: " + reason + "]");
        return serviceBookingRepository.save(booking);
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
