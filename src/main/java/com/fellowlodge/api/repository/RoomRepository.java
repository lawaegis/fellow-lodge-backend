package com.fellowlodge.api.repository;

import com.fellowlodge.api.entity.Room;
import com.fellowlodge.api.enums.RoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID>, JpaSpecificationExecutor<Room> {

    Optional<Room> findByRoomNumberIgnoreCase(String roomNumber);

    List<Room> findByStatus(RoomStatus status);

    List<Room> findByFloor(int floor);

    List<Room> findByRoomTypeId(UUID roomTypeId);

    List<Room> findByStatusOrderByRoomNumber(RoomStatus status);

    long countByStatus(RoomStatus status);

    boolean existsByRoomNumberIgnoreCase(String roomNumber);
}
