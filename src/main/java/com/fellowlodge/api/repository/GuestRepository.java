package com.fellowlodge.api.repository;

import com.fellowlodge.api.entity.Guest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GuestRepository extends JpaRepository<Guest, UUID>, JpaSpecificationExecutor<Guest> {

    Optional<Guest> findByEmailIgnoreCase(String email);

    Optional<Guest> findByUserId(UUID userId);

    List<Guest> findByVipTrue();

    @Query("""
            select g from Guest g
            where lower(concat(g.firstName, ' ', g.lastName)) like lower(concat('%', :query, '%'))
               or lower(coalesce(g.email, '')) like lower(concat('%', :query, '%'))
               or lower(coalesce(g.phone, '')) like lower(concat('%', :query, '%'))
               or lower(coalesce(g.idNumber, '')) like lower(concat('%', :query, '%'))
            """)
    List<Guest> search(@Param("query") String query);
}
