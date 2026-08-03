package com.fellowlodge.api.controller;

import com.fellowlodge.api.common.ApiResponse;
import com.fellowlodge.api.common.PageResponse;
import com.fellowlodge.api.entity.Housekeeping;
import com.fellowlodge.api.service.HousekeepingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/housekeeping")
@RequiredArgsConstructor
public class HousekeepingController {

    private final HousekeepingService housekeepingService;

    @GetMapping
    @PreAuthorize("hasAuthority('HOUSEKEEPING:READ')")
    public ApiResponse<List<Housekeeping>> findAll(@RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size,
                                                   @RequestParam(required = false) String sort,
                                                   @RequestParam(required = false) String status,
                                                   @RequestParam(required = false) String priority,
                                                   @RequestParam(required = false) UUID roomId,
                                                   @RequestParam(required = false) UUID assignedTo,
                                                   @RequestParam(required = false)
                                                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate scheduledDate) {
        Page<Housekeeping> result = housekeepingService
                .findAll(page, size, sort, status, priority, roomId, assignedTo, scheduledDate);
        return ApiResponse.ok(result.getContent(), PageResponse.from(result));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('HOUSEKEEPING:READ')")
    public ApiResponse<Housekeeping> findById(@PathVariable UUID id) {
        return ApiResponse.ok(housekeepingService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('HOUSEKEEPING:WRITE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Housekeeping> create(@Valid @RequestBody Housekeeping task) {
        return ApiResponse.created("Housekeeping task created", housekeepingService.create(task));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('HOUSEKEEPING:WRITE')")
    public ApiResponse<Housekeeping> update(@PathVariable UUID id, @Valid @RequestBody Housekeeping task) {
        return ApiResponse.ok("Housekeeping updated", housekeepingService.update(id, task));
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAuthority('HOUSEKEEPING:WRITE')")
    public ApiResponse<Housekeeping> assign(@PathVariable UUID id, @RequestParam UUID staffId) {
        return ApiResponse.ok("Task assigned", housekeepingService.assign(id, staffId));
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasAuthority('HOUSEKEEPING:WRITE')")
    public ApiResponse<Housekeeping> start(@PathVariable UUID id) {
        return ApiResponse.ok("Task started", housekeepingService.start(id));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('HOUSEKEEPING:WRITE')")
    public ApiResponse<Housekeeping> complete(@PathVariable UUID id) {
        return ApiResponse.ok("Task completed", housekeepingService.complete(id));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('HOUSEKEEPING:WRITE')")
    public ApiResponse<Housekeeping> cancel(@PathVariable UUID id, @RequestParam(required = false) String reason) {
        return ApiResponse.ok("Task cancelled", housekeepingService.cancel(id, reason));
    }
}
