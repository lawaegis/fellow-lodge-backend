package com.fellowlodge.api.controller;

import com.fellowlodge.api.common.ApiResponse;
import com.fellowlodge.api.common.PageResponse;
import com.fellowlodge.api.entity.ConferenceHall;
import com.fellowlodge.api.service.ConferenceHallService;
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
@RequestMapping("/api/conference-halls")
@RequiredArgsConstructor
public class ConferenceHallController {

    private final ConferenceHallService conferenceHallService;

    @GetMapping
    @PreAuthorize("hasAuthority('CONFERENCE_HALLS:READ')")
    public ApiResponse<List<ConferenceHall>> findAll(@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "20") int size,
                                                     @RequestParam(required = false) String sort,
                                                     @RequestParam(required = false) Boolean active) {
        Page<ConferenceHall> result = conferenceHallService.findAll(page, size, sort, active);
        return ApiResponse.ok(result.getContent(), PageResponse.from(result));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('CONFERENCE_HALLS:READ')")
    public ApiResponse<List<ConferenceHall>> findAll() {
        return ApiResponse.ok(conferenceHallService.findAll());
    }

    @GetMapping("/{id}/availability")
    public ApiResponse<com.fellowlodge.api.dto.portal.AvailabilityResponse> availability(@PathVariable UUID id) {
        return ApiResponse.ok(conferenceHallService.availability(id));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CONFERENCE_HALLS:READ')")
    public ApiResponse<ConferenceHall> findById(@PathVariable UUID id) {
        return ApiResponse.ok(conferenceHallService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CONFERENCE_HALLS:WRITE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ConferenceHall> create(@Valid @RequestBody ConferenceHall hall) {
        return ApiResponse.created("Conference hall created", conferenceHallService.create(hall));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CONFERENCE_HALLS:WRITE')")
    public ApiResponse<ConferenceHall> update(@PathVariable UUID id, @Valid @RequestBody ConferenceHall hall) {
        return ApiResponse.ok("Conference hall updated", conferenceHallService.update(id, hall));
    }

    @PostMapping("/{id}/active")
    @PreAuthorize("hasAuthority('CONFERENCE_HALLS:WRITE')")
    public ApiResponse<ConferenceHall> setActive(@PathVariable UUID id, @RequestParam boolean active) {
        return ApiResponse.ok("Conference hall updated", conferenceHallService.setActive(id, active));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CONFERENCE_HALLS:DELETE')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        conferenceHallService.delete(id);
        return ApiResponse.deleted("Conference hall deleted");
    }
}
