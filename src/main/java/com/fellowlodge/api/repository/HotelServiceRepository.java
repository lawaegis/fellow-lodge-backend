package com.fellowlodge.api.repository;

import com.fellowlodge.api.entity.HotelService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface HotelServiceRepository extends JpaRepository<HotelService, UUID>, JpaSpecificationExecutor<HotelService> {

    List<HotelService> findByActiveTrue();

    List<HotelService> findByCategoryIgnoreCase(String category);
}
