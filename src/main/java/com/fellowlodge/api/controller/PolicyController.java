package com.fellowlodge.api.controller;

import com.fellowlodge.api.common.ApiResponse;
import com.fellowlodge.api.common.PageResponse;
import com.fellowlodge.api.entity.Policy;
import com.fellowlodge.api.service.PolicyService;
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
@RequestMapping("/api/policies")
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyService policyService;

    @GetMapping
    @PreAuthorize("hasAuthority('POLICIES:READ')")
    public ApiResponse<List<Policy>> findAll(@RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "20") int size,
                                             @RequestParam(required = false) String sort,
                                             @RequestParam(required = false) Boolean active) {
        Page<Policy> result = policyService.findAll(page, size, sort, active);
        return ApiResponse.ok(result.getContent(), PageResponse.from(result));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('POLICIES:READ')")
    public ApiResponse<List<Policy>> findAll() {
        return ApiResponse.ok(policyService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('POLICIES:READ')")
    public ApiResponse<Policy> findById(@PathVariable UUID id) {
        return ApiResponse.ok(policyService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('POLICIES:WRITE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Policy> create(@Valid @RequestBody Policy policy) {
        return ApiResponse.created("Policy created", policyService.create(policy));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('POLICIES:WRITE')")
    public ApiResponse<Policy> update(@PathVariable UUID id, @Valid @RequestBody Policy policy) {
        return ApiResponse.ok("Policy updated", policyService.update(id, policy));
    }

    @PostMapping("/{id}/active")
    @PreAuthorize("hasAuthority('POLICIES:WRITE')")
    public ApiResponse<Policy> setActive(@PathVariable UUID id, @RequestParam boolean active) {
        return ApiResponse.ok("Policy updated", policyService.setActive(id, active));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('POLICIES:DELETE')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        policyService.delete(id);
        return ApiResponse.deleted("Policy deleted");
    }
}
