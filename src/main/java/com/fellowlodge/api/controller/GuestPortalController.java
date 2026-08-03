package com.fellowlodge.api.controller;

import com.fellowlodge.api.common.ApiResponse;
import com.fellowlodge.api.dto.portal.SupportTicketRequest;
import com.fellowlodge.api.entity.GuestPreference;
import com.fellowlodge.api.entity.SupportTicket;
import com.fellowlodge.api.security.SecurityUtils;
import com.fellowlodge.api.service.GuestPreferenceService;
import com.fellowlodge.api.service.SupportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Guest self-service endpoints for the portal: stay preferences and support
 * tickets. Every request is resolved to the currently authenticated user, so
 * guests can only ever read or write their own data.
 */
@RestController
@RequestMapping("/api/guest")
@RequiredArgsConstructor
public class GuestPortalController {

    private final GuestPreferenceService preferenceService;
    private final SupportService supportService;

    @GetMapping("/preferences")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<GuestPreference> preferences() {
        GuestPreference preferences = preferenceService.findByUserId(SecurityUtils.currentUserId());
        return ApiResponse.ok(preferences == null ? new GuestPreference() : preferences);
    }

    @PutMapping("/preferences")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<GuestPreference> savePreferences(@Valid @RequestBody GuestPreference preferences) {
        return ApiResponse.ok("Preferences saved", preferenceService.upsert(SecurityUtils.currentUserId(), preferences));
    }

    @PostMapping("/support")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SupportTicket> createSupportTicket(@Valid @RequestBody SupportTicketRequest request) {
        return ApiResponse.created("Support ticket created",
                supportService.create(request, SecurityUtils.currentUserId()));
    }

    @GetMapping("/support")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<SupportTicket>> myTickets() {
        UUID userId = SecurityUtils.currentUserId();
        return ApiResponse.ok(supportService.findByUserId(userId));
    }
}
