package com.fellowlodge.api.service;

import com.fellowlodge.api.common.exception.InvalidOperationException;
import com.fellowlodge.api.common.exception.ResourceNotFoundException;
import com.fellowlodge.api.entity.Housekeeping;
import com.fellowlodge.api.entity.Room;
import com.fellowlodge.api.enums.HousekeepingStatus;
import com.fellowlodge.api.enums.PriorityLevel;
import com.fellowlodge.api.enums.RoomStatus;
import com.fellowlodge.api.repository.HousekeepingRepository;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HousekeepingService {

    private final HousekeepingRepository housekeepingRepository;
    private final RoomRepository roomRepository;

    public Page<Housekeeping> findAll(int page, int size, String sort, String status, String priority,
                                      UUID roomId, UUID assignedTo, LocalDate scheduledDate) {
        Pageable pageable = PageRequest.of(page, size, buildSort(sort));
        Specification<Housekeeping> spec = Specification.where(null);
        if (StringUtils.hasText(status)) {
            HousekeepingStatus hs = HousekeepingStatus.valueOf(status);
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("status"), hs));
        }
        if (StringUtils.hasText(priority)) {
            PriorityLevel pl = PriorityLevel.valueOf(priority);
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("priority"), pl));
        }
        if (roomId != null) {
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("roomId"), roomId));
        }
        if (assignedTo != null) {
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("assignedTo"), assignedTo));
        }
        if (scheduledDate != null) {
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("scheduledDate"), scheduledDate));
        }
        return housekeepingRepository.findAll(spec, pageable);
    }

    public Housekeeping findById(UUID id) {
        return housekeepingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Housekeeping task", id));
    }

    public long countByStatus(HousekeepingStatus status) {
        return housekeepingRepository.countByStatus(status);
    }

    @Transactional
    public Housekeeping create(Housekeeping task) {
        if (task.getStatus() == null) {
            task.setStatus(HousekeepingStatus.Pending);
        }
        return housekeepingRepository.save(task);
    }

    @Transactional
    public Housekeeping update(UUID id, Housekeeping updated) {
        Housekeeping task = findById(id);
        updated.setId(id);
        updated.setStatus(task.getStatus());
        updated.setCompletedDate(task.getCompletedDate());
        return housekeepingRepository.save(updated);
    }

    @Transactional
    public Housekeeping assign(UUID id, UUID staffId) {
        Housekeeping task = findById(id);
        if (task.getStatus() == HousekeepingStatus.Completed
                || task.getStatus() == HousekeepingStatus.Cancelled) {
            throw new InvalidOperationException("Completed or cancelled tasks cannot be reassigned.");
        }
        task.setAssignedTo(staffId);
        return housekeepingRepository.save(task);
    }

    @Transactional
    public Housekeeping start(UUID id) {
        Housekeeping task = findById(id);
        if (task.getStatus() != HousekeepingStatus.Pending) {
            throw new InvalidOperationException("Only pending tasks can be started.");
        }
        task.setStatus(HousekeepingStatus.InProgress);
        return housekeepingRepository.save(task);
    }

    @Transactional
    public Housekeeping complete(UUID id) {
        Housekeeping task = findById(id);
        if (task.getStatus() == HousekeepingStatus.Completed
                || task.getStatus() == HousekeepingStatus.Cancelled) {
            throw new InvalidOperationException("Task is already closed.");
        }
        task.setStatus(HousekeepingStatus.Completed);
        task.setCompletedDate(LocalDateTime.now());
        if (task.getRoomId() != null) {
            roomRepository.findById(task.getRoomId()).ifPresent(room -> {
                if (room.getStatus() == RoomStatus.Cleaning) {
                    room.setStatus(RoomStatus.Available);
                    roomRepository.save(room);
                }
            });
        }
        return housekeepingRepository.save(task);
    }

    @Transactional
    public Housekeeping cancel(UUID id, String reason) {
        Housekeeping task = findById(id);
        if (task.getStatus() == HousekeepingStatus.Completed) {
            throw new InvalidOperationException("Completed tasks cannot be cancelled.");
        }
        task.setStatus(HousekeepingStatus.Cancelled);
        task.setNotes(task.getNotes() == null ? reason : task.getNotes() + " [Cancelled: " + reason + "]");
        return housekeepingRepository.save(task);
    }

    private Sort buildSort(String sort) {
        if (!StringUtils.hasText(sort)) {
            return Sort.by(Sort.Direction.ASC, "scheduledDate");
        }
        String[] parts = sort.split(",");
        Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1])
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, parts[0]);
    }
}
