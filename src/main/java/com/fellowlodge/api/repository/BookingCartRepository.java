package com.fellowlodge.api.repository;

import com.fellowlodge.api.entity.BookingCart;
import com.fellowlodge.api.enums.BookingCartStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BookingCartRepository extends JpaRepository<BookingCart, UUID> {

    Optional<BookingCart> findByUserIdAndStatus(UUID userId, BookingCartStatus status);

    Optional<BookingCart> findByGuestIdAndStatus(UUID guestId, BookingCartStatus status);

    Optional<BookingCart> findBySessionIdAndStatus(String sessionId, BookingCartStatus status);
}
