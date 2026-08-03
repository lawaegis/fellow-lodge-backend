package com.fellowlodge.api.repository;

import com.fellowlodge.api.entity.ConferenceHall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface ConferenceHallRepository extends JpaRepository<ConferenceHall, UUID>, JpaSpecificationExecutor<ConferenceHall> {

    List<ConferenceHall> findByActiveTrue();
}
