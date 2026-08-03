package com.fellowlodge.api.repository;

import com.fellowlodge.api.entity.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CheckInRepository extends JpaRepository<CheckIn, UUID> {

    Optional<CheckIn> findByReservationId(UUID reservationId);

    List<CheckIn> findByRoomId(UUID roomId);
}
