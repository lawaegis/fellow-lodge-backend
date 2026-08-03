package com.fellowlodge.api.controller;

import com.fellowlodge.api.common.ApiResponse;
import com.fellowlodge.api.common.PageResponse;
import com.fellowlodge.api.entity.Notification;
import com.fellowlodge.api.security.SecurityUtils;
import com.fellowlodge.api.service.NotificationService;
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
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @PreAuthorize("hasAuthority('NOTIFICATIONS:READ')")
    public ApiResponse<List<Notification>> findAll(@RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size) {
        Page<Notification> result = notificationService.findAll(page, size);
        return ApiResponse.ok(result.getContent(), PageResponse.from(result));
    }

    @GetMapping("/my")
    public ApiResponse<List<Notification>> mine() {
        UUID userId = SecurityUtils.currentUserId();
        return ApiResponse.ok(notificationService.findByUserId(userId));
    }

    @GetMapping("/unread-count")
    public ApiResponse<Map<String, Long>> unreadCount() {
        return ApiResponse.ok(Map.of("count", notificationService.countUnreadByUserId(SecurityUtils.currentUserId())));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('NOTIFICATIONS:READ')")
    public ApiResponse<List<Notification>> byUser(@PathVariable UUID userId) {
        return ApiResponse.ok(notificationService.findByUserId(userId));
    }

    @GetMapping("/guest/{guestId}")
    @PreAuthorize("hasAuthority('NOTIFICATIONS:READ')")
    public ApiResponse<List<Notification>> byGuest(@PathVariable UUID guestId) {
        return ApiResponse.ok(notificationService.findByGuestId(guestId));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('NOTIFICATIONS:WRITE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Notification> create(@Valid @RequestBody Notification notification) {
        return ApiResponse.created("Notification created", notificationService.create(notification));
    }

    @PostMapping("/{id}/read")
    public ApiResponse<Void> markRead(@PathVariable UUID id) {
        notificationService.markAsRead(id, SecurityUtils.currentUserId());
        return ApiResponse.<Void>ok("Notification marked as read", null);
    }

    @PostMapping("/read-all")
    public ApiResponse<Void> markAllRead() {
        notificationService.markAllAsRead(SecurityUtils.currentUserId());
        return ApiResponse.<Void>ok("All notifications marked as read", null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('NOTIFICATIONS:WRITE')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        notificationService.delete(id);
        return ApiResponse.deleted("Notification deleted");
    }
}
