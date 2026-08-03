package com.fellowlodge.api.controller;

import com.fellowlodge.api.common.ApiResponse;
import com.fellowlodge.api.common.PageResponse;
import com.fellowlodge.api.entity.ConferencePackage;
import com.fellowlodge.api.service.ConferencePackageService;
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
@RequestMapping("/api/conference-packages")
@RequiredArgsConstructor
public class ConferencePackageController {

    private final ConferencePackageService conferencePackageService;

    @GetMapping
    @PreAuthorize("hasAuthority('CONFERENCE_PACKAGES:READ')")
    public ApiResponse<List<ConferencePackage>> findAll(@RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "20") int size,
                                                        @RequestParam(required = false) String sort,
                                                        @RequestParam(required = false) Boolean active) {
        Page<ConferencePackage> result = conferencePackageService.findAll(page, size, sort, active);
        return ApiResponse.ok(result.getContent(), PageResponse.from(result));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('CONFERENCE_PACKAGES:READ')")
    public ApiResponse<List<ConferencePackage>> findAll() {
        return ApiResponse.ok(conferencePackageService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CONFERENCE_PACKAGES:READ')")
    public ApiResponse<ConferencePackage> findById(@PathVariable UUID id) {
        return ApiResponse.ok(conferencePackageService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CONFERENCE_PACKAGES:WRITE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ConferencePackage> create(@Valid @RequestBody ConferencePackage conferencePackage) {
        return ApiResponse.created("Conference package created", conferencePackageService.create(conferencePackage));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CONFERENCE_PACKAGES:WRITE')")
    public ApiResponse<ConferencePackage> update(@PathVariable UUID id, @Valid @RequestBody ConferencePackage conferencePackage) {
        return ApiResponse.ok("Conference package updated", conferencePackageService.update(id, conferencePackage));
    }

    @PostMapping("/{id}/active")
    @PreAuthorize("hasAuthority('CONFERENCE_PACKAGES:WRITE')")
    public ApiResponse<ConferencePackage> setActive(@PathVariable UUID id, @RequestParam boolean active) {
        return ApiResponse.ok("Conference package updated", conferencePackageService.setActive(id, active));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CONFERENCE_PACKAGES:DELETE')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        conferencePackageService.delete(id);
        return ApiResponse.deleted("Conference package deleted");
    }
}
