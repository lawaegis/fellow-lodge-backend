package com.fellowlodge.api.controller;

import com.fellowlodge.api.common.ApiResponse;
import com.fellowlodge.api.common.PageResponse;
import com.fellowlodge.api.entity.Review;
import com.fellowlodge.api.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping
    @PreAuthorize("hasAuthority('REVIEWS:READ')")
    public ApiResponse<List<Review>> findAll(@RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "20") int size,
                                             @RequestParam(required = false) String sort,
                                             @RequestParam(required = false) String status,
                                             @RequestParam(required = false) UUID roomId,
                                             @RequestParam(required = false) UUID guestId) {
        Page<Review> result = reviewService.findAll(page, size, sort, status, roomId, guestId);
        return ApiResponse.ok(result.getContent(), PageResponse.from(result));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('REVIEWS:READ')")
    public ApiResponse<Review> findById(@PathVariable UUID id) {
        return ApiResponse.ok(reviewService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('REVIEWS:WRITE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Review> create(@Valid @RequestBody Review review) {
        return ApiResponse.created("Review submitted", reviewService.create(review));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('REVIEWS:WRITE')")
    public ApiResponse<Review> approve(@PathVariable UUID id) {
        return ApiResponse.ok("Review approved", reviewService.approve(id));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('REVIEWS:WRITE')")
    public ApiResponse<Review> reject(@PathVariable UUID id) {
        return ApiResponse.ok("Review rejected", reviewService.reject(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('REVIEWS:DELETE')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        reviewService.delete(id);
        return ApiResponse.deleted("Review deleted");
    }

    @GetMapping("/average-rating")
    public ApiResponse<Map<String, Double>> averageRating() {
        return ApiResponse.ok(Map.of("average", reviewService.averageRating()));
    }
}
