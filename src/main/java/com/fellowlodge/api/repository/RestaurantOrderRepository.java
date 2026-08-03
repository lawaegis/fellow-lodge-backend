package com.fellowlodge.api.repository;

import com.fellowlodge.api.entity.RestaurantOrder;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RestaurantOrderRepository extends JpaRepository<RestaurantOrder, UUID> {

    @EntityGraph(attributePaths = "items")
    List<RestaurantOrder> findByUserIdOrderByPlacedAtDesc(UUID userId);

    @EntityGraph(attributePaths = "items")
    List<RestaurantOrder> findByGuestIdOrderByPlacedAtDesc(UUID guestId);

    @EntityGraph(attributePaths = "items")
    List<RestaurantOrder> findAllByOrderByPlacedAtDesc();

    @EntityGraph(attributePaths = "items")
    Optional<RestaurantOrder> findWithItemsById(UUID id);

    boolean existsByOrderNumber(String orderNumber);
}
