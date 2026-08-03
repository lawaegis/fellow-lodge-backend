package com.fellowlodge.api.repository;

import com.fellowlodge.api.entity.Event;
import com.fellowlodge.api.enums.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID>, JpaSpecificationExecutor<Event> {

    List<Event> findByStatus(EventStatus status);

    List<Event> findByEventDateAfterOrderByEventDateAsc(LocalDateTime date);
}
