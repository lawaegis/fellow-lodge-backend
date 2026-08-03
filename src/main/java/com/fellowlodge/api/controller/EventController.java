package com.fellowlodge.api.controller;

import com.fellowlodge.api.common.ApiResponse;
import com.fellowlodge.api.common.PageResponse;
import com.fellowlodge.api.entity.Event;
import com.fellowlodge.api.enums.EventStatus;
import com.fellowlodge.api.security.SecurityUtils;
import com.fellowlodge.api.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping
    @PreAuthorize("hasAuthority('EVENTS:READ')")
    public ApiResponse<List<Event>> findAll(@RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "20") int size,
                                            @RequestParam(required = false) String sort,
                                            @RequestParam(required = false) String status) {
        Page<Event> result = eventService.findAll(page, size, sort, status);
        return ApiResponse.ok(result.getContent(), PageResponse.from(result));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('EVENTS:READ')")
    public ApiResponse<List<Event>> findAll() {
        return ApiResponse.ok(eventService.findAll());
    }

    @GetMapping("/upcoming")
    public ApiResponse<List<Event>> upcoming() {
        return ApiResponse.ok(eventService.findUpcoming());
    }

    @GetMapping("/{id}/availability")
    public ApiResponse<com.fellowlodge.api.dto.portal.AvailabilityResponse> availability(@PathVariable UUID id) {
        return ApiResponse.ok(eventService.availability(id));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('EVENTS:READ')")
    public ApiResponse<Event> findById(@PathVariable UUID id) {
        return ApiResponse.ok(eventService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('EVENTS:WRITE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Event> create(@Valid @RequestBody Event event) {
        return ApiResponse.created("Event created", eventService.create(event, SecurityUtils.currentUserId()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('EVENTS:WRITE')")
    public ApiResponse<Event> update(@PathVariable UUID id, @Valid @RequestBody Event event) {
        return ApiResponse.ok("Event updated", eventService.update(id, event));
    }

    @PostMapping("/{id}/status")
    @PreAuthorize("hasAuthority('EVENTS:WRITE')")
    public ApiResponse<Event> setStatus(@PathVariable UUID id, @RequestParam EventStatus status) {
        return ApiResponse.ok("Event status updated", eventService.setStatus(id, status));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('EVENTS:WRITE')")
    public ApiResponse<Event> cancel(@PathVariable UUID id) {
        return ApiResponse.ok("Event cancelled", eventService.cancel(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('EVENTS:DELETE')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        eventService.delete(id);
        return ApiResponse.deleted("Event deleted");
    }
}
