package com.fellowlodge.api.controller;

import com.fellowlodge.api.common.ApiResponse;
import com.fellowlodge.api.common.PageResponse;
import com.fellowlodge.api.entity.EventPackage;
import com.fellowlodge.api.service.EventPackageService;
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

@RestController
@RequestMapping("/api/event-packages")
@RequiredArgsConstructor
public class EventPackageController {

    private final EventPackageService eventPackageService;

    @GetMapping
    @PreAuthorize("hasAuthority('EVENT_PACKAGES:READ')")
    public ApiResponse<List<EventPackage>> findAll(@RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size,
                                                   @RequestParam(required = false) String sort,
                                                   @RequestParam(required = false) Boolean active) {
        Page<EventPackage> result = eventPackageService.findAll(page, size, sort, active);
        return ApiResponse.ok(result.getContent(), PageResponse.from(result));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('EVENT_PACKAGES:READ')")
    public ApiResponse<List<EventPackage>> findAll() {
        return ApiResponse.ok(eventPackageService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('EVENT_PACKAGES:READ')")
    public ApiResponse<EventPackage> findById(@PathVariable UUID id) {
        return ApiResponse.ok(eventPackageService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('EVENT_PACKAGES:WRITE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<EventPackage> create(@Valid @RequestBody EventPackage eventPackage) {
        return ApiResponse.created("Event package created", eventPackageService.create(eventPackage));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('EVENT_PACKAGES:WRITE')")
    public ApiResponse<EventPackage> update(@PathVariable UUID id, @Valid @RequestBody EventPackage eventPackage) {
        return ApiResponse.ok("Event package updated", eventPackageService.update(id, eventPackage));
    }

    @PostMapping("/{id}/active")
    @PreAuthorize("hasAuthority('EVENT_PACKAGES:WRITE')")
    public ApiResponse<EventPackage> setActive(@PathVariable UUID id, @RequestParam boolean active) {
        return ApiResponse.ok("Event package updated", eventPackageService.setActive(id, active));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('EVENT_PACKAGES:DELETE')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        eventPackageService.delete(id);
        return ApiResponse.deleted("Event package deleted");
    }
}
