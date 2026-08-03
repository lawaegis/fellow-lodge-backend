package com.fellowlodge.api.repository;

import com.fellowlodge.api.entity.RoomImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface RoomImageRepository extends JpaRepository<RoomImage, UUID> {

    List<RoomImage> findByRoomId(UUID roomId);

    List<RoomImage> findByRoomIdIn(Collection<UUID> roomIds);

    List<RoomImage> findByRoomTypeId(UUID roomTypeId);
}
