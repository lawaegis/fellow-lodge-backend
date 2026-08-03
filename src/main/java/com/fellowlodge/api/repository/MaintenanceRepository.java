package com.fellowlodge.api.repository;

import com.fellowlodge.api.entity.Maintenance;
import com.fellowlodge.api.enums.MaintenanceStatus;
import com.fellowlodge.api.enums.PriorityLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface MaintenanceRepository extends JpaRepository<Maintenance, UUID>, JpaSpecificationExecutor<Maintenance> {

    List<Maintenance> findByRoomId(UUID roomId);

    List<Maintenance> findByStatus(MaintenanceStatus status);

    List<Maintenance> findByPriority(PriorityLevel priority);

    long countByStatus(MaintenanceStatus status);
}
