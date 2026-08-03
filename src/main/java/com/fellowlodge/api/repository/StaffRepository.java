package com.fellowlodge.api.repository;

import com.fellowlodge.api.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StaffRepository extends JpaRepository<Staff, UUID>, JpaSpecificationExecutor<Staff> {

    Optional<Staff> findByUserId(UUID userId);

    List<Staff> findByDepartmentIgnoreCase(String department);

    @Query("""
            select s from Staff s
            where lower(concat(s.firstName, ' ', s.lastName)) like lower(concat('%', :query, '%'))
               or lower(coalesce(s.email, '')) like lower(concat('%', :query, '%'))
               or lower(coalesce(s.phone, '')) like lower(concat('%', :query, '%'))
               or lower(coalesce(s.position, '')) like lower(concat('%', :query, '%'))
            """)
    List<Staff> search(@Param("query") String query);
}
