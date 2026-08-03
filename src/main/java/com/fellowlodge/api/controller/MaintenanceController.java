package com.fellowlodge.api.controller;

import com.fellowlodge.api.common.ApiResponse;
import com.fellowlodge.api.common.PageResponse;
import com.fellowlodge.api.entity.Maintenance;
import com.fellowlodge.api.security.SecurityUtils;
import com.fellowlodge.api.service.MaintenanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/maintenance")
@RequiredArgsConstructor
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    @GetMapping
    @PreAuthorize("hasAuthority('MAINTENANCE:READ')")
    public ApiResponse<List<Maintenance>> findAll(@RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "20") int size,
                                                  @RequestParam(required = false) String sort,
                                                  @RequestParam(required = false) String status,
                                                  @RequestParam(required = false) String priority,
                                                  @RequestParam(required = false) UUID roomId) {
        Page<Maintenance> result = maintenanceService.findAll(page, size, sort, status, priority, roomId);
        return ApiResponse.ok(result.getContent(), PageResponse.from(result));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MAINTENANCE:READ')")
    public ApiResponse<Maintenance> findById(@PathVariable UUID id) {
        return ApiResponse.ok(maintenanceService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MAINTENANCE:WRITE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Maintenance> create(@Valid @RequestBody Maintenance maintenance) {
        return ApiResponse.created("Maintenance request created",
                maintenanceService.create(maintenance, SecurityUtils.currentUserId()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MAINTENANCE:WRITE')")
    public ApiResponse<Maintenance> update(@PathVariable UUID id, @Valid @RequestBody Maintenance maintenance) {
        return ApiResponse.ok("Maintenance updated", maintenanceService.update(id, maintenance));
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAuthority('MAINTENANCE:WRITE')")
    public ApiResponse<Maintenance> assign(@PathVariable UUID id, @RequestParam UUID staffId) {
        return ApiResponse.ok("Maintenance assigned", maintenanceService.assign(id, staffId));
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasAuthority('MAINTENANCE:WRITE')")
    public ApiResponse<Maintenance> start(@PathVariable UUID id) {
        return ApiResponse.ok("Maintenance started", maintenanceService.start(id));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('MAINTENANCE:WRITE')")
    public ApiResponse<Maintenance> complete(@PathVariable UUID id,
                                            @RequestParam(required = false) BigDecimal actualCost) {
        return ApiResponse.ok("Maintenance completed", maintenanceService.complete(id, actualCost));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('MAINTENANCE:WRITE')")
    public ApiResponse<Maintenance> cancel(@PathVariable UUID id, @RequestParam(required = false) String reason) {
        return ApiResponse.ok("Maintenance cancelled", maintenanceService.cancel(id, reason));
    }
}
