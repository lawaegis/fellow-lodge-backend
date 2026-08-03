package com.fellowlodge.api.repository;

import com.fellowlodge.api.entity.Review;
import com.fellowlodge.api.enums.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID>, JpaSpecificationExecutor<Review> {

    List<Review> findByStatus(ReviewStatus status);

    List<Review> findByRoomId(UUID roomId);

    List<Review> findByGuestId(UUID guestId);
}
