package com.fellowlodge.api.controller;

import com.fellowlodge.api.common.ApiResponse;
import com.fellowlodge.api.common.PageResponse;
import com.fellowlodge.api.entity.Announcement;
import com.fellowlodge.api.security.SecurityUtils;
import com.fellowlodge.api.service.AnnouncementService;
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
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @GetMapping
    @PreAuthorize("hasAuthority('ANNOUNCEMENTS:READ')")
    public ApiResponse<List<Announcement>> findAll(@RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size,
                                                   @RequestParam(required = false) String sort,
                                                   @RequestParam(required = false) Boolean active) {
        Page<Announcement> result = announcementService.findAll(page, size, sort, active);
        return ApiResponse.ok(result.getContent(), PageResponse.from(result));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ANNOUNCEMENTS:READ')")
    public ApiResponse<List<Announcement>> findAll() {
        return ApiResponse.ok(announcementService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ANNOUNCEMENTS:READ')")
    public ApiResponse<Announcement> findById(@PathVariable UUID id) {
        return ApiResponse.ok(announcementService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ANNOUNCEMENTS:WRITE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Announcement> create(@Valid @RequestBody Announcement announcement) {
        return ApiResponse.created("Announcement created",
                announcementService.create(announcement, SecurityUtils.currentUserId()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ANNOUNCEMENTS:WRITE')")
    public ApiResponse<Announcement> update(@PathVariable UUID id, @Valid @RequestBody Announcement announcement) {
        return ApiResponse.ok("Announcement updated", announcementService.update(id, announcement));
    }

    @PostMapping("/{id}/active")
    @PreAuthorize("hasAuthority('ANNOUNCEMENTS:WRITE')")
    public ApiResponse<Announcement> setActive(@PathVariable UUID id, @RequestParam boolean active) {
        return ApiResponse.ok("Announcement updated", announcementService.setActive(id, active));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ANNOUNCEMENTS:DELETE')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        announcementService.delete(id);
        return ApiResponse.deleted("Announcement deleted");
    }
}
