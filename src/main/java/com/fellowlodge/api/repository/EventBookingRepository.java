package com.fellowlodge.api.repository;

import com.fellowlodge.api.entity.EventBooking;
import com.fellowlodge.api.enums.EventBookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface EventBookingRepository extends JpaRepository<EventBooking, UUID>, JpaSpecificationExecutor<EventBooking> {

    List<EventBooking> findByGuestId(UUID guestId);

    List<EventBooking> findByEventId(UUID eventId);

    List<EventBooking> findByConferenceHallId(UUID conferenceHallId);

    List<EventBooking> findByStatus(EventBookingStatus status);
}
