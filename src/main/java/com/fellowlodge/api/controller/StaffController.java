package com.fellowlodge.api.controller;

import com.fellowlodge.api.common.ApiResponse;
import com.fellowlodge.api.common.PageResponse;
import com.fellowlodge.api.entity.Staff;
import com.fellowlodge.api.service.StaffService;
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
@RequestMapping("/api/staff")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;

    @GetMapping
    @PreAuthorize("hasAuthority('STAFF:READ')")
    public ApiResponse<List<Staff>> findAll(@RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "20") int size,
                                            @RequestParam(required = false) String sort,
                                            @RequestParam(required = false) String search,
                                            @RequestParam(required = false) String department) {
        Page<Staff> result = staffService.findAll(page, size, sort, search, department);
        return ApiResponse.ok(result.getContent(), PageResponse.from(result));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('STAFF:READ')")
    public ApiResponse<List<Staff>> findAll() {
        return ApiResponse.ok(staffService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('STAFF:READ')")
    public ApiResponse<Staff> findById(@PathVariable UUID id) {
        return ApiResponse.ok(staffService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('STAFF:WRITE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Staff> create(@Valid @RequestBody Staff staff) {
        return ApiResponse.created("Staff created", staffService.create(staff));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('STAFF:WRITE')")
    public ApiResponse<Staff> update(@PathVariable UUID id, @Valid @RequestBody Staff staff) {
        return ApiResponse.ok("Staff updated", staffService.update(id, staff));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('STAFF:DELETE')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        staffService.delete(id);
        return ApiResponse.deleted("Staff deleted");
    }
}
