package com.fellowlodge.api.service;

import com.fellowlodge.api.common.exception.InvalidOperationException;
import com.fellowlodge.api.common.exception.ResourceNotFoundException;
import com.fellowlodge.api.dto.booking.BookingRequest;
import com.fellowlodge.api.dto.booking.CancelRequest;
import com.fellowlodge.api.dto.checkin.CheckInRequest;
import com.fellowlodge.api.dto.checkin.CheckOutRequest;
import com.fellowlodge.api.entity.CheckIn;
import com.fellowlodge.api.entity.CheckOut;
import com.fellowlodge.api.entity.Invoice;
import com.fellowlodge.api.entity.Reservation;
import com.fellowlodge.api.entity.Room;
import com.fellowlodge.api.entity.RoomType;
import com.fellowlodge.api.entity.Transaction;
import com.fellowlodge.api.enums.InvoiceStatus;
import com.fellowlodge.api.enums.ReservationSource;
import com.fellowlodge.api.enums.ReservationStatus;
import com.fellowlodge.api.enums.RoomStatus;
import com.fellowlodge.api.enums.TransactionStatus;
import com.fellowlodge.api.enums.TransactionType;
import com.fellowlodge.api.repository.CheckInRepository;
import com.fellowlodge.api.repository.CheckOutRepository;
import com.fellowlodge.api.repository.InvoiceRepository;
import com.fellowlodge.api.repository.ReservationRepository;
import com.fellowlodge.api.repository.RoomRepository;
import com.fellowlodge.api.repository.RoomTypeRepository;
import com.fellowlodge.api.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final CheckInRepository checkInRepository;
    private final CheckOutRepository checkOutRepository;
    private final InvoiceRepository invoiceRepository;
    private final TransactionRepository transactionRepository;
    private final NotificationService notificationService;

    // ==================== QUERIES ====================

    public Page<Reservation> findAll(int page, int size, String sort, String search, String status,
                                     UUID guestId, UUID roomId, LocalDate from, LocalDate to) {
        Pageable pageable = PageRequest.of(page, size, buildSort(sort));
        Specification<Reservation> spec = Specification.where(null);
        if (StringUtils.hasText(search)) {
            spec = spec.and((root, q, cb) -> cb.or(
                    cb.like(cb.lower(root.get("specialRequests")), "%" + search.toLowerCase() + "%")));
        }
        if (StringUtils.hasText(status)) {
            ReservationStatus rs = ReservationStatus.valueOf(status);
            spec = spec.and((root, q, cb) -> cb.equal(root.get("status"), rs));
        }
        if (guestId != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("guestId"), guestId));
        }
        if (roomId != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("roomId"), roomId));
        }
        if (from != null) {
            spec = spec.and((root, q, cb) -> cb.greaterThanOrEqualTo(root.get("checkInDate"), from));
        }
        if (to != null) {
            spec = spec.and((root, q, cb) -> cb.lessThanOrEqualTo(root.get("checkOutDate"), to));
        }
        return reservationRepository.findAll(spec, pageable);
    }

    public List<Reservation> findAll() {
        return reservationRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public Reservation findById(UUID id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", id));
    }

    public List<Reservation> findByGuestId(UUID guestId) {
        return reservationRepository.findByGuestId(guestId);
    }

    public List<Reservation> findByStatus(ReservationStatus status) {
        return reservationRepository.findByStatus(status);
    }

    public List<Reservation> findTodayCheckIns() {
        return reservationRepository.findByStatusAndCheckInDate(ReservationStatus.Confirmed, LocalDate.now());
    }

    public List<Reservation> findTodayCheckOuts() {
        return reservationRepository.findByStatusAndCheckOutDate(ReservationStatus.CheckedIn, LocalDate.now());
    }

    public long count() {
        return reservationRepository.count();
    }

    public long countByStatus(ReservationStatus status) {
        return reservationRepository.countByStatus(status);
    }

    public boolean hasOverlap(UUID roomId, LocalDate checkIn, LocalDate checkOut, UUID excludeId) {
        return reservationRepository.hasOverlap(roomId, checkIn, checkOut, excludeId);
    }

    // ==================== MUTATIONS ====================

    @Transactional
    public Reservation create(Reservation reservation, UUID bookedBy) {
        validateDates(reservation.getCheckInDate(), reservation.getCheckOutDate());
        if (reservation.getGuestId() == null) {
            throw new InvalidOperationException("A guest is required to create a reservation.");
        }
        resolveRoom(reservation);
        if (reservation.getRoomId() != null
                && reservationRepository.hasOverlap(reservation.getRoomId(),
                reservation.getCheckInDate(), reservation.getCheckOutDate(), null)) {
            throw new InvalidOperationException("The room is already reserved for the selected dates.");
        }
        if (reservation.getTotalAmount() == null || reservation.getTotalAmount().signum() == 0) {
            reservation.setTotalAmount(computeTotal(reservation));
        }
        reservation.setBookedBy(bookedBy);
        reservation.setStatus(reservation.getStatus() == null ? ReservationStatus.Pending : reservation.getStatus());
        reservation.setSource(reservation.getSource() == null ? ReservationSource.DESKTOP : reservation.getSource());
        Reservation saved = reservationRepository.save(reservation);
        notificationService.notifyAdmins("New reservation", "Reservation " + saved.getId() + " received for "
                + saved.getCheckInDate());
        return saved;
    }

    @Transactional
    public Reservation createFromRequest(BookingRequest request, UUID bookedBy) {
        Reservation reservation = new Reservation();
        reservation.setGuestId(request.guestId());
        reservation.setRoomTypeId(request.roomTypeId());
        reservation.setRoomId(request.roomId());
        reservation.setCheckInDate(request.checkInDate());
        reservation.setCheckOutDate(request.checkOutDate());
        reservation.setNumberOfGuests(request.numberOfGuests());
        reservation.setSpecialRequests(request.specialRequests());
        reservation.setSource(StringUtils.hasText(request.source())
                ? ReservationSource.valueOf(request.source())
                : ReservationSource.DESKTOP);
        return create(reservation, bookedBy);
    }

    @Transactional
    public Reservation update(UUID id, Reservation updated) {
        Reservation reservation = findById(id);
        if (reservation.getStatus() == ReservationStatus.CheckedIn
                || reservation.getStatus() == ReservationStatus.CheckedOut) {
            throw new InvalidOperationException("Cannot modify a reservation that is already checked in.");
        }
        updated.setId(id);
        updated.setBookedBy(reservation.getBookedBy());
        updated.setSource(reservation.getSource());
        updated.setCreatedAt(reservation.getCreatedAt());
        if (updated.getStatus() == null) {
            updated.setStatus(reservation.getStatus());
        }
        return reservationRepository.save(updated);
    }

    @Transactional
    public void delete(UUID id) {
        Reservation reservation = findById(id);
        if (reservation.getStatus() == ReservationStatus.CheckedIn) {
            throw new InvalidOperationException("Cannot delete an in-house reservation. Check out first.");
        }
        reservationRepository.delete(reservation);
    }

    @Transactional
    public Reservation approve(UUID id) {
        Reservation reservation = findById(id);
        if (reservation.getStatus() != ReservationStatus.Pending) {
            throw new InvalidOperationException("Only pending reservations can be approved.");
        }
        reservation.setStatus(ReservationStatus.Confirmed);
        Reservation saved = reservationRepository.save(reservation);
        markRoomReserved(saved);
        notificationService.notifyAdmins("Reservation approved", "Reservation " + id + " was approved.");
        return saved;
    }

    @Transactional
    public Reservation cancel(UUID id, CancelRequest request) {
        return cancel(id, request.reason());
    }

    @Transactional
    public Reservation cancel(UUID id, String reason) {
        Reservation reservation = findById(id);
        if (reservation.getStatus() == ReservationStatus.CheckedIn
                || reservation.getStatus() == ReservationStatus.CheckedOut) {
            throw new InvalidOperationException("Checked-in reservations cannot be cancelled.");
        }
        reservation.setStatus(ReservationStatus.Cancelled);
        reservation.setCancellationReason(reason);
        if (reservation.getRoomId() != null) {
            roomRepository.findById(reservation.getRoomId()).ifPresent(room -> {
                if (room.getStatus() == RoomStatus.Reserved) {
                    room.setStatus(RoomStatus.Available);
                    roomRepository.save(room);
                }
            });
        }
        Reservation saved = reservationRepository.save(reservation);
        notificationService.notifyGuest(saved.getGuestId(), "Booking cancelled",
                "Your reservation " + id + " was cancelled.");
        return saved;
    }

    // ==================== CHECK-IN / CHECK-OUT WORKFLOW ====================

    @Transactional
    public CheckIn checkIn(UUID reservationId, CheckInRequest request, UUID checkedInBy) {
        Reservation reservation = findById(reservationId);
        if (reservation.getStatus() != ReservationStatus.Confirmed
                && reservation.getStatus() != ReservationStatus.Pending) {
            throw new InvalidOperationException("Reservation must be confirmed before check-in.");
        }
        Room room = reservation.getRoomId() == null ? null : roomRepository.findById(reservation.getRoomId())
                .orElse(null);
        if (room == null) {
            throw new InvalidOperationException("No room assigned to this reservation.");
        }
        if (room.getStatus() == RoomStatus.Occupied) {
            throw new InvalidOperationException("Room " + room.getRoomNumber() + " is already occupied.");
        }

        CheckIn checkIn = new CheckIn();
        checkIn.setReservationId(reservationId);
        checkIn.setRoomId(room.getId());
        checkIn.setGuestId(reservation.getGuestId());
        checkIn.setCheckedInBy(checkedInBy);
        checkIn.setCheckInTime(LocalDateTime.now());
        checkIn.setRoomCondition(request.roomCondition() == null ? "Good" : request.roomCondition());
        checkIn.setNotes(request.notes());
        CheckIn saved = checkInRepository.save(checkIn);

        reservation.setStatus(ReservationStatus.CheckedIn);
        reservation.setActualCheckIn(LocalDateTime.now());
        reservationRepository.save(reservation);

        room.setStatus(RoomStatus.Occupied);
        roomRepository.save(room);

        notificationService.notifyAdmins("Guest checked in",
                "Guest checked in to room " + room.getRoomNumber());
        return saved;
    }

    @Transactional
    public CheckOut checkOut(UUID checkInId, CheckOutRequest request, UUID checkedOutBy) {
        CheckIn checkIn = checkInRepository.findById(checkInId)
                .orElseThrow(() -> new ResourceNotFoundException("Check-in record", checkInId));
        if (checkOutRepository.findByCheckInId(checkInId).isPresent()) {
            throw new InvalidOperationException("This guest has already checked out.");
        }
        Reservation reservation = reservationRepository.findById(checkIn.getReservationId())
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", checkIn.getReservationId()));

        BigDecimal miniBar = orZero(request.miniBarCharges());
        BigDecimal damage = orZero(request.damageCharges());
        BigDecimal other = orZero(request.otherCharges());
        BigDecimal totalAdditional = miniBar.add(damage).add(other);

        CheckOut checkOut = new CheckOut();
        checkOut.setCheckInId(checkInId);
        checkOut.setCheckedOutBy(checkedOutBy);
        checkOut.setCheckOutTime(LocalDateTime.now());
        checkOut.setRoomCondition(request.roomCondition() == null ? "Good" : request.roomCondition());
        checkOut.setMiniBarCharges(miniBar);
        checkOut.setDamageCharges(damage);
        checkOut.setOtherCharges(other);
        checkOut.setTotalAdditionalCharges(totalAdditional);
        checkOut.setNotes(request.notes());
        CheckOut saved = checkOutRepository.save(checkOut);

        reservation.setStatus(ReservationStatus.CheckedOut);
        reservation.setActualCheckOut(LocalDateTime.now());
        reservation.setTotalAmount(orZero(reservation.getTotalAmount()).add(totalAdditional));
        reservationRepository.save(reservation);

        if (reservation.getRoomId() != null) {
            roomRepository.findById(reservation.getRoomId()).ifPresent(room -> {
                room.setStatus(RoomStatus.Cleaning);
                room.setLastMaintained(LocalDateTime.now());
                roomRepository.save(room);
            });
        }

        Invoice invoice = invoiceRepository.findByReservationId(reservation.getId()).stream()
                .findFirst()
                .orElseGet(() -> generateInvoice(reservation));
        invoice.setSubtotal(reservation.getTotalAmount());
        invoice.setTotalAmount(reservation.getTotalAmount());
        invoiceRepository.save(invoice);

        notificationService.notifyGuest(reservation.getGuestId(), "Check-out complete",
                "You have checked out successfully. Total: " + invoice.getTotalAmount());
        return saved;
    }

    // ==================== HELPERS ====================

    public BigDecimal computeTotal(Reservation reservation) {
        BigDecimal nights = BigDecimal.valueOf(nightsBetween(reservation.getCheckInDate(), reservation.getCheckOutDate()));
        BigDecimal rate;
        if (reservation.getRoomId() != null) {
            Room room = roomRepository.findById(reservation.getRoomId()).orElse(null);
            rate = room != null ? room.getPricePerNight() : BigDecimal.ZERO;
        } else if (reservation.getRoomTypeId() != null) {
            RoomType type = roomTypeRepository.findById(reservation.getRoomTypeId()).orElse(null);
            rate = type != null ? type.getBasePrice() : BigDecimal.ZERO;
        } else {
            rate = BigDecimal.ZERO;
        }
        BigDecimal discount = orZero(reservation.getDiscountPercent());
        BigDecimal multiplier = BigDecimal.ONE.subtract(
                discount.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
        return nights.multiply(rate).multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
    }

    public static long nightsBetween(LocalDate checkIn, LocalDate checkOut) {
        return java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
    }

    private void resolveRoom(Reservation reservation) {
        if (reservation.getRoomId() != null) {
            Room room = roomRepository.findById(reservation.getRoomId())
                    .orElseThrow(() -> new ResourceNotFoundException("Room", reservation.getRoomId()));
            reservation.setRoomTypeId(room.getRoomTypeId());
        }
    }

    private void markRoomReserved(Reservation reservation) {
        if (reservation.getRoomId() == null) {
            return;
        }
        roomRepository.findById(reservation.getRoomId()).ifPresent(room -> {
            if (room.getStatus() == RoomStatus.Available) {
                room.setStatus(RoomStatus.Reserved);
                roomRepository.save(room);
            }
        });
    }

    private Invoice generateInvoice(Reservation reservation) {
        Invoice invoice = new Invoice();
        invoice.setReservationId(reservation.getId());
        invoice.setGuestId(reservation.getGuestId());
        invoice.setInvoiceNumber(generateInvoiceNumber());
        invoice.setSubtotal(orZero(reservation.getTotalAmount()));
        invoice.setTaxAmount(BigDecimal.ZERO);
        invoice.setDiscountAmount(BigDecimal.ZERO);
        invoice.setTotalAmount(orZero(reservation.getTotalAmount()));
        invoice.setStatus(InvoiceStatus.Sent);
        invoice.setDueDate(LocalDate.now().plusDays(14));
        return invoiceRepository.save(invoice);
    }

    private String generateInvoiceNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        String candidate;
        do {
            candidate = "INV-" + timestamp + "-" + random;
        } while (invoiceRepository.findByInvoiceNumber(candidate).isPresent());
        return candidate;
    }

    private void validateDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new InvalidOperationException("Check-in and check-out dates are required.");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new InvalidOperationException("Check-out date must be after the check-in date.");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new InvalidOperationException("Check-in date cannot be in the past.");
        }
    }

    private BigDecimal orZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private Sort buildSort(String sort) {
        if (!StringUtils.hasText(sort)) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        String[] parts = sort.split(",");
        Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1])
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, parts[0]);
    }
}
