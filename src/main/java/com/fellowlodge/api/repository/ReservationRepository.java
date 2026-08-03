package com.fellowlodge.api.repository;

import com.fellowlodge.api.entity.Reservation;
import com.fellowlodge.api.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID>, JpaSpecificationExecutor<Reservation> {

    List<Reservation> findByGuestId(UUID guestId);

    List<Reservation> findByRoomId(UUID roomId);

    List<Reservation> findByStatus(ReservationStatus status);

    List<Reservation> findByCheckInDate(LocalDate date);

    List<Reservation> findByCheckOutDate(LocalDate date);

    List<Reservation> findByStatusAndCheckInDate(ReservationStatus status, LocalDate date);

    List<Reservation> findByStatusAndCheckOutDate(ReservationStatus status, LocalDate date);

    long countByStatus(ReservationStatus status);

    @Query("""
            select case when count(r) > 0 then true else false end
            from Reservation r
            where r.roomId = :roomId
              and r.checkInDate < :checkOut
              and r.checkOutDate > :checkIn
              and r.status not in (com.fellowlodge.api.enums.ReservationStatus.Cancelled,
                                   com.fellowlodge.api.enums.ReservationStatus.NoShow,
                                   com.fellowlodge.api.enums.ReservationStatus.CheckedOut)
              and (:excludeReservationId is null or r.id <> :excludeReservationId)
            """)
    boolean hasOverlap(@Param("roomId") UUID roomId,
                       @Param("checkIn") LocalDate checkIn,
                       @Param("checkOut") LocalDate checkOut,
                       @Param("excludeReservationId") UUID excludeReservationId);

    @Query("""
            select distinct r.roomId
            from Reservation r
            where r.roomId in :roomIds
              and r.checkInDate < :checkOut
              and r.checkOutDate > :checkIn
              and r.status not in (com.fellowlodge.api.enums.ReservationStatus.Cancelled,
                                   com.fellowlodge.api.enums.ReservationStatus.NoShow,
                                   com.fellowlodge.api.enums.ReservationStatus.CheckedOut)
            """)
    Set<UUID> findOverlappingRoomIds(@Param("roomIds") Collection<UUID> roomIds,
                                     @Param("checkIn") LocalDate checkIn,
                                     @Param("checkOut") LocalDate checkOut);

    @Query("""
            select r from Reservation r
            where r.roomId = :roomId
              and r.status in (com.fellowlodge.api.enums.ReservationStatus.Confirmed,
                               com.fellowlodge.api.enums.ReservationStatus.CheckedIn,
                               com.fellowlodge.api.enums.ReservationStatus.Pending)
            order by r.checkInDate asc
            """)
    List<Reservation> findActiveForRoom(@Param("roomId") UUID roomId);
}
