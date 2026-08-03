package com.fellowlodge.api.repository;

import com.fellowlodge.api.entity.CheckOut;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CheckOutRepository extends JpaRepository<CheckOut, UUID> {

    Optional<CheckOut> findByCheckInId(UUID checkInId);
}
