package com.fellowlodge.api.controller;

import com.fellowlodge.api.common.ApiResponse;
import com.fellowlodge.api.common.PageResponse;
import com.fellowlodge.api.entity.Attraction;
import com.fellowlodge.api.service.AttractionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Admin CRUD for local attractions. Changes appear immediately on the guest
 * portal via /public/hotel/attractions (single source of truth = PostgreSQL).
 */
@RestController
@RequestMapping("/api/attractions")
@RequiredArgsConstructor
public class AttractionController {

    private final AttractionService attractionService;

    @GetMapping
    @PreAuthorize("hasAuthority('ATTRACTIONS:READ')")
    public ApiResponse<List<Attraction>> findAll(@RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size,
                                                 @RequestParam(required = false) String sort,
                                                 @RequestParam(required = false) Boolean active) {
        Page<Attraction> result = attractionService.findAll(page, size, sort, active);
        return ApiResponse.ok(result.getContent(), PageResponse.from(result));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ATTRACTIONS:READ')")
    public ApiResponse<List<Attraction>> findAll() {
        return ApiResponse.ok(attractionService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ATTRACTIONS:READ')")
    public ApiResponse<Attraction> findById(@PathVariable UUID id) {
        return ApiResponse.ok(attractionService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ATTRACTIONS:WRITE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Attraction> create(@Valid @RequestBody Attraction attraction) {
        return ApiResponse.created("Attraction created", attractionService.create(attraction));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ATTRACTIONS:WRITE')")
    public ApiResponse<Attraction> update(@PathVariable UUID id, @Valid @RequestBody Attraction attraction) {
        return ApiResponse.ok("Attraction updated", attractionService.update(id, attraction));
    }

    @PostMapping("/{id}/active")
    @PreAuthorize("hasAuthority('ATTRACTIONS:WRITE')")
    public ApiResponse<Attraction> setActive(@PathVariable UUID id, @RequestParam boolean active) {
        return ApiResponse.ok("Attraction updated", attractionService.setActive(id, active));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ATTRACTIONS:DELETE')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        attractionService.delete(id);
        return ApiResponse.deleted("Attraction deleted");
    }
}
