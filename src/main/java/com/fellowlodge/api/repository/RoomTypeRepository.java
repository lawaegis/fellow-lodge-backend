package com.fellowlodge.api.repository;

import com.fellowlodge.api.entity.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoomTypeRepository extends JpaRepository<RoomType, UUID>, JpaSpecificationExecutor<RoomType> {

    Optional<RoomType> findByNameIgnoreCase(String name);

    List<RoomType> findByActiveTrue();

    List<RoomType> findByIdIn(Collection<UUID> ids);

    boolean existsByNameIgnoreCase(String name);
}
