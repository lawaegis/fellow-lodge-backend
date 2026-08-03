package com.fellowlodge.api.service;

import com.fellowlodge.api.common.exception.ResourceNotFoundException;
import com.fellowlodge.api.entity.CheckIn;
import com.fellowlodge.api.repository.CheckInRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CheckInService {

    private final CheckInRepository checkInRepository;

    public CheckIn findById(UUID id) {
        return checkInRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Check-in record", id));
    }

    public List<CheckIn> findAll() {
        return checkInRepository.findAll();
    }

    public CheckIn findByReservationId(UUID reservationId) {
        return checkInRepository.findByReservationId(reservationId).orElse(null);
    }

    public List<CheckIn> findByRoomId(UUID roomId) {
        return checkInRepository.findByRoomId(roomId);
    }
}
