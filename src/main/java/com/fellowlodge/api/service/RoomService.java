package com.fellowlodge.api.service;

import com.fellowlodge.api.common.exception.DuplicateResourceException;
import com.fellowlodge.api.common.exception.InvalidOperationException;
import com.fellowlodge.api.common.exception.ResourceNotFoundException;
import com.fellowlodge.api.entity.Room;
import com.fellowlodge.api.enums.RoomStatus;
import com.fellowlodge.api.repository.ReservationRepository;
import com.fellowlodge.api.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;

    public Page<Room> findAll(int page, int size, String sort, String search, String status, UUID roomTypeId,
                              Integer floor) {
        Pageable pageable = PageRequest.of(page, size, buildSort(sort));
        Specification<Room> spec = Specification.where(null);
        if (StringUtils.hasText(search)) {
            String like = "%" + search.toLowerCase() + "%";
            spec = spec.and((root, q, cb) -> cb.or(
                    cb.like(cb.lower(root.get("roomNumber")), like),
                    cb.like(cb.lower(cb.coalesce(root.get("description"), "")), like)));
        }
        if (StringUtils.hasText(status)) {
            spec = spec.and((root, q, cb) ->
                    cb.equal(root.get("status"), RoomStatus.valueOf(status)));
        }
        if (roomTypeId != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("roomTypeId"), roomTypeId));
        }
        if (floor != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("floor"), floor));
        }
        return roomRepository.findAll(spec, pageable);
    }

    public List<Room> findAll() {
        return roomRepository.findAll(Sort.by("roomNumber"));
    }

    public Room findById(UUID id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room", id));
    }

    public Room findByRoomNumber(String roomNumber) {
        return roomRepository.findByRoomNumberIgnoreCase(roomNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Room with number " + roomNumber));
    }

    public List<Room> findByStatus(RoomStatus status) {
        return roomRepository.findByStatusOrderByRoomNumber(status);
    }

    public List<Room> findAvailable() {
        return roomRepository.findByStatusOrderByRoomNumber(RoomStatus.Available);
    }

    public List<Room> findAvailableForDates(LocalDate checkIn, LocalDate checkOut) {
        List<Room> rooms = roomRepository.findAll().stream()
                .filter(room -> room.getStatus() == RoomStatus.Available)
                .sorted(java.util.Comparator.comparing(Room::getRoomNumber))
                .toList();
        if (rooms.isEmpty() || checkIn == null || checkOut == null) {
            return rooms;
        }
        Set<UUID> unavailable = reservationRepository.findOverlappingRoomIds(
                rooms.stream().map(Room::getId).toList(), checkIn, checkOut);
        return rooms.stream().filter(room -> !unavailable.contains(room.getId())).toList();
    }

    public List<Room> findByRoomTypeId(UUID roomTypeId) {
        return roomRepository.findByRoomTypeId(roomTypeId);
    }

    @Transactional
    public Room create(Room room) {
        if (roomRepository.existsByRoomNumberIgnoreCase(room.getRoomNumber())) {
            throw new DuplicateResourceException("Room number already exists: " + room.getRoomNumber());
        }
        if (room.getPricePerNight() == null) {
            throw new InvalidOperationException("Price per night is required.");
        }
        Room saved = roomRepository.save(room);
        audit("CREATE", saved.getId());
        return saved;
    }

    @Transactional
    public Room update(UUID id, Room updated) {
        Room room = findById(id);
        updated.setId(id);
        roomRepository.findByRoomNumberIgnoreCase(updated.getRoomNumber())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("Room number already exists: " + updated.getRoomNumber());
                });
        copyFields(room, updated);
        Room saved = roomRepository.save(room);
        audit("UPDATE", saved.getId());
        return saved;
    }

    @Transactional
    public void delete(UUID id) {
        Room room = findById(id);
        if (room.getStatus() == RoomStatus.Occupied) {
            throw new InvalidOperationException("Cannot delete an occupied room.");
        }
        roomRepository.delete(room);
        audit("DELETE", id);
    }

    @Transactional
    public Room changeStatus(UUID id, RoomStatus status) {
        Room room = findById(id);
        room.setStatus(status);
        if (status == RoomStatus.Maintenance || status == RoomStatus.Cleaning) {
            room.setLastMaintained(java.time.LocalDateTime.now());
        }
        Room saved = roomRepository.save(room);
        audit("STATUS_CHANGE:" + status.name(), saved.getId());
        return saved;
    }

    public long count() {
        return roomRepository.count();
    }

    public long countByStatus(RoomStatus status) {
        return roomRepository.countByStatus(status);
    }

    private Sort buildSort(String sort) {
        if (!StringUtils.hasText(sort)) {
            return Sort.by("roomNumber").ascending();
        }
        String[] parts = sort.split(",");
        String property = parts[0];
        Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1])
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, property);
    }

    private void copyFields(Room target, Room source) {
        target.setRoomNumber(source.getRoomNumber());
        target.setRoomTypeId(source.getRoomTypeId());
        target.setFloor(source.getFloor());
        target.setStatus(source.getStatus());
        target.setPricePerNight(source.getPricePerNight());
        target.setExtraCharges(source.getExtraCharges());
        target.setDescription(source.getDescription());
        target.setHasBalcony(source.isHasBalcony());
        target.setHasView(source.isHasView());
        target.setSmoking(source.isSmoking());
        target.setAccessible(source.isAccessible());
        target.setNotes(source.getNotes());
        target.setImageUrl(source.getImageUrl());
        target.setLastMaintained(source.getLastMaintained());
    }

    private void audit(String action, UUID recordId) {
        // Audited by the controller-level AuditAspect where enabled.
    }
}
