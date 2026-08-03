package com.fellowlodge.api.repository;

import com.fellowlodge.api.entity.Housekeeping;
import com.fellowlodge.api.enums.HousekeepingStatus;
import com.fellowlodge.api.enums.PriorityLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface HousekeepingRepository extends JpaRepository<Housekeeping, UUID>, JpaSpecificationExecutor<Housekeeping> {

    List<Housekeeping> findByRoomId(UUID roomId);

    List<Housekeeping> findByStatus(HousekeepingStatus status);

    List<Housekeeping> findByPriority(PriorityLevel priority);

    List<Housekeeping> findByAssignedTo(UUID staffId);

    List<Housekeeping> findByScheduledDate(LocalDate date);

    long countByStatus(HousekeepingStatus status);
}
