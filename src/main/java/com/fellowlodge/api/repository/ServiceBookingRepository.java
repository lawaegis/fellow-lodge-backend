package com.fellowlodge.api.repository;

import com.fellowlodge.api.entity.ServiceBooking;
import com.fellowlodge.api.enums.ServiceBookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface ServiceBookingRepository extends JpaRepository<ServiceBooking, UUID>, JpaSpecificationExecutor<ServiceBooking> {

    List<ServiceBooking> findByGuestId(UUID guestId);

    List<ServiceBooking> findByStatus(ServiceBookingStatus status);
}
