package com.fellowlodge.api.controller;

import com.fellowlodge.api.common.ApiResponse;
import com.fellowlodge.api.entity.SupportTicket;
import com.fellowlodge.api.enums.SupportTicketPriority;
import com.fellowlodge.api.enums.SupportTicketStatus;
import com.fellowlodge.api.service.SupportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Staff support-ticket management. Tickets are submitted by portal guests via
 * /api/guest/support; staff list them here and move them through the workflow
 * (Open -> InProgress -> Resolved -> Closed).
 */
@RestController
@RequestMapping("/api/support-tickets")
@RequiredArgsConstructor
public class SupportTicketController {

    private final SupportService supportService;

    @GetMapping
    @PreAuthorize("hasAuthority('SUPPORT:READ')")
    public ApiResponse<List<SupportTicket>> findAll(@RequestParam(required = false) String status,
                                                    @RequestParam(required = false) String priority) {
        return ApiResponse.ok(supportService.findAll());
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('SUPPORT:READ')")
    public ApiResponse<Map<String, Long>> stats() {
        return ApiResponse.ok(Map.of("open", supportService.countOpen()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPPORT:READ')")
    public ApiResponse<SupportTicket> findById(@PathVariable UUID id) {
        return ApiResponse.ok(supportService.findById(id));
    }

    @PostMapping("/{id}/status")
    @PreAuthorize("hasAuthority('SUPPORT:WRITE')")
    public ApiResponse<SupportTicket> setStatus(@PathVariable UUID id,
                                                @RequestParam SupportTicketStatus status) {
        return ApiResponse.ok("Ticket status updated", supportService.setStatus(id, status));
    }

    @PostMapping("/{id}/priority")
    @PreAuthorize("hasAuthority('SUPPORT:WRITE')")
    public ApiResponse<SupportTicket> setPriority(@PathVariable UUID id,
                                                  @RequestParam SupportTicketPriority priority) {
        return ApiResponse.ok("Ticket priority updated", supportService.setPriority(id, priority));
    }
}
