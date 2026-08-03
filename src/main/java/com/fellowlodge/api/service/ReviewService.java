package com.fellowlodge.api.service;

import com.fellowlodge.api.common.exception.InvalidOperationException;
import com.fellowlodge.api.common.exception.ResourceNotFoundException;
import com.fellowlodge.api.entity.Review;
import com.fellowlodge.api.enums.ReviewStatus;
import com.fellowlodge.api.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public Page<Review> findAll(int page, int size, String sort, String status, UUID roomId, UUID guestId) {
        Pageable pageable = PageRequest.of(page, size, buildSort(sort));
        Specification<Review> spec = Specification.where(null);
        if (StringUtils.hasText(status)) {
            ReviewStatus rs = ReviewStatus.valueOf(status);
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("status"), rs));
        }
        if (roomId != null) {
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("roomId"), roomId));
        }
        if (guestId != null) {
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("guestId"), guestId));
        }
        return reviewRepository.findAll(spec, pageable);
    }

    public List<Review> findByStatus(ReviewStatus status) {
        return reviewRepository.findByStatus(status);
    }

    public Review findById(UUID id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review", id));
    }

    public double averageRating() {
        List<Review> approved = reviewRepository.findByStatus(ReviewStatus.Approved);
        if (approved.isEmpty()) {
            return 0;
        }
        return approved.stream().mapToInt(Review::getRating).average().orElse(0);
    }

    @Transactional
    public Review create(Review review) {
        if (review.getRating() < 1 || review.getRating() > 5) {
            throw new InvalidOperationException("Rating must be between 1 and 5.");
        }
        review.setStatus(ReviewStatus.Pending);
        return reviewRepository.save(review);
    }

    @Transactional
    public Review update(UUID id, Review updated) {
        Review review = findById(id);
        if (updated.getRating() < 1 || updated.getRating() > 5) {
            throw new InvalidOperationException("Rating must be between 1 and 5.");
        }
        review.setRating(updated.getRating());
        if (updated.getComment() != null) {
            review.setComment(updated.getComment());
        }
        if (updated.getRoomId() != null) {
            review.setRoomId(updated.getRoomId());
        }
        review.setStatus(ReviewStatus.Pending);
        return reviewRepository.save(review);
    }

    @Transactional
    public Review approve(UUID id) {
        Review review = findById(id);
        review.setStatus(ReviewStatus.Approved);
        return reviewRepository.save(review);
    }

    @Transactional
    public Review reject(UUID id) {
        Review review = findById(id);
        review.setStatus(ReviewStatus.Rejected);
        return reviewRepository.save(review);
    }

    @Transactional
    public void delete(UUID id) {
        reviewRepository.delete(findById(id));
    }

    private Sort buildSort(String sort) {
        if (!StringUtils.hasText(sort)) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        String[] parts = sort.split(",");
        Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1])
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, parts[0]);
    }
}
