package com.fellowlodge.api.repository;

import com.fellowlodge.api.entity.RestaurantOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RestaurantOrderItemRepository extends JpaRepository<RestaurantOrderItem, UUID> {
}
