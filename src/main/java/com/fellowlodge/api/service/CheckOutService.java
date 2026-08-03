package com.fellowlodge.api.service;

import com.fellowlodge.api.common.exception.ResourceNotFoundException;
import com.fellowlodge.api.entity.CheckOut;
import com.fellowlodge.api.repository.CheckOutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CheckOutService {

    private final CheckOutRepository checkOutRepository;

    public CheckOut findById(UUID id) {
        return checkOutRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Check-out record", id));
    }

    public List<CheckOut> findAll() {
        return checkOutRepository.findAll();
    }

    public CheckOut findByCheckInId(UUID checkInId) {
        return checkOutRepository.findByCheckInId(checkInId).orElse(null);
    }
}
