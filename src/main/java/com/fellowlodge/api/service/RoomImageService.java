package com.fellowlodge.api.service;

import com.fellowlodge.api.common.exception.ResourceNotFoundException;
import com.fellowlodge.api.entity.RoomImage;
import com.fellowlodge.api.repository.RoomImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoomImageService {

    private final RoomImageRepository roomImageRepository;

    public List<RoomImage> findByRoomId(UUID roomId) {
        return roomImageRepository.findByRoomId(roomId);
    }

    public List<RoomImage> findByRoomTypeId(UUID roomTypeId) {
        return roomImageRepository.findByRoomTypeId(roomTypeId);
    }

    @Transactional
    public RoomImage add(RoomImage image) {
        if (image.isPrimary()) {
            clearPrimaryFlags(image.getRoomId(), image.getRoomTypeId());
        }
        return roomImageRepository.save(image);
    }

    @Transactional
    public RoomImage setPrimary(UUID id) {
        RoomImage image = roomImageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room image", id));
        clearPrimaryFlags(image.getRoomId(), image.getRoomTypeId());
        image.setPrimary(true);
        return roomImageRepository.save(image);
    }

    @Transactional
    public void delete(UUID id) {
        roomImageRepository.deleteById(id);
    }

    private void clearPrimaryFlags(UUID roomId, UUID roomTypeId) {
        if (roomId != null) {
            roomImageRepository.findByRoomId(roomId)
                    .forEach(img -> img.setPrimary(false));
        }
        if (roomTypeId != null) {
            roomImageRepository.findByRoomTypeId(roomTypeId)
                    .forEach(img -> img.setPrimary(false));
        }
    }
}
