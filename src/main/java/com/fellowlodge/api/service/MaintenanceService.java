package com.fellowlodge.api.service;

import com.fellowlodge.api.common.exception.InvalidOperationException;
import com.fellowlodge.api.common.exception.ResourceNotFoundException;
import com.fellowlodge.api.entity.Maintenance;
import com.fellowlodge.api.entity.Room;
import com.fellowlodge.api.enums.MaintenanceStatus;
import com.fellowlodge.api.enums.PriorityLevel;
import com.fellowlodge.api.enums.RoomStatus;
import com.fellowlodge.api.repository.MaintenanceRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MaintenanceService {

    private final MaintenanceRepository maintenanceRepository;
    private final RoomRepository roomRepository;

    public Page<Maintenance> findAll(int page, int size, String sort, String status, String priority, UUID roomId) {
        Pageable pageable = PageRequest.of(page, size, buildSort(sort));
        Specification<Maintenance> spec = Specification.where(null);
        if (StringUtils.hasText(status)) {
            MaintenanceStatus ms = MaintenanceStatus.valueOf(status);
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("status"), ms));
        }
        if (StringUtils.hasText(priority)) {
            PriorityLevel pl = PriorityLevel.valueOf(priority);
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("priority"), pl));
        }
        if (roomId != null) {
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("roomId"), roomId));
        }
        return maintenanceRepository.findAll(spec, pageable);
    }

    public Maintenance findById(UUID id) {
        return maintenanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance record", id));
    }

    public long countByStatus(MaintenanceStatus status) {
        return maintenanceRepository.countByStatus(status);
    }

    @Transactional
    public Maintenance create(Maintenance maintenance, UUID reportedBy) {
        maintenance.setReportedBy(reportedBy);
        maintenance.setReportedDate(LocalDateTime.now());
        maintenance.setStatus(MaintenanceStatus.Reported);
        return maintenanceRepository.save(maintenance);
    }

    @Transactional
    public Maintenance update(UUID id, Maintenance updated) {
        Maintenance record = findById(id);
        updated.setId(id);
        updated.setReportedBy(record.getReportedBy());
        updated.setReportedDate(record.getReportedDate());
        updated.setStatus(record.getStatus());
        updated.setCompletedDate(record.getCompletedDate());
        return maintenanceRepository.save(updated);
    }

    @Transactional
    public Maintenance assign(UUID id, UUID staffId) {
        Maintenance record = findById(id);
        if (record.getStatus() == MaintenanceStatus.Completed
                || record.getStatus() == MaintenanceStatus.Cancelled) {
            throw new InvalidOperationException("Completed or cancelled records cannot be reassigned.");
        }
        record.setAssignedTo(staffId);
        return maintenanceRepository.save(record);
    }

    @Transactional
    public Maintenance start(UUID id) {
        Maintenance record = findById(id);
        if (record.getStatus() != MaintenanceStatus.Reported) {
            throw new InvalidOperationException("Only reported records can be started.");
        }
        record.setStatus(MaintenanceStatus.InProgress);
        return maintenanceRepository.save(record);
    }

    @Transactional
    public Maintenance complete(UUID id, java.math.BigDecimal actualCost) {
        Maintenance record = findById(id);
        if (record.getStatus() == MaintenanceStatus.Completed
                || record.getStatus() == MaintenanceStatus.Cancelled) {
            throw new InvalidOperationException("Record is already closed.");
        }
        record.setStatus(MaintenanceStatus.Completed);
        record.setActualCost(actualCost);
        record.setCompletedDate(LocalDateTime.now());
        if (record.getRoomId() != null) {
            roomRepository.findById(record.getRoomId()).ifPresent(room -> {
                if (room.getStatus() == RoomStatus.OutOfService) {
                    room.setStatus(RoomStatus.Available);
                    roomRepository.save(room);
                }
            });
        }
        return maintenanceRepository.save(record);
    }

    @Transactional
    public Maintenance cancel(UUID id, String reason) {
        Maintenance record = findById(id);
        if (record.getStatus() == MaintenanceStatus.Completed) {
            throw new InvalidOperationException("Completed records cannot be cancelled.");
        }
        record.setStatus(MaintenanceStatus.Cancelled);
        record.setDescription(record.getDescription() + (reason == null ? "" : " [Cancelled: " + reason + "]"));
        return maintenanceRepository.save(record);
    }

    private Sort buildSort(String sort) {
        if (!StringUtils.hasText(sort)) {
            return Sort.by(Sort.Direction.DESC, "reportedDate");
        }
        String[] parts = sort.split(",");
        Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1])
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, parts[0]);
    }
}
