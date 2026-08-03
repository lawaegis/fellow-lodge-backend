package com.fellowlodge.api.repository;

import com.fellowlodge.api.entity.BookingItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BookingItemRepository extends JpaRepository<BookingItem, UUID> {

    List<BookingItem> findByCartId(UUID cartId);

    void deleteByCartId(UUID cartId);
}
