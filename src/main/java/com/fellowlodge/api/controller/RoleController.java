package com.fellowlodge.api.controller;

import com.fellowlodge.api.common.ApiResponse;
import com.fellowlodge.api.entity.Permission;
import com.fellowlodge.api.entity.Role;
import com.fellowlodge.api.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLES:READ')")
    public ApiResponse<List<Role>> findAll() {
        return ApiResponse.ok(roleService.findAll());
    }

    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('ROLES:READ')")
    public ApiResponse<List<Permission>> permissions() {
        return ApiResponse.ok(roleService.findAllPermissions());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLES:READ')")
    public ApiResponse<Role> findById(@PathVariable UUID id) {
        return ApiResponse.ok(roleService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLES:WRITE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Role> create(@Valid @RequestBody Role role) {
        return ApiResponse.created("Role created", roleService.create(role));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLES:WRITE')")
    public ApiResponse<Role> update(@PathVariable UUID id, @Valid @RequestBody Role role) {
        return ApiResponse.ok("Role updated", roleService.update(id, role));
    }

    @PutMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('ROLES:WRITE')")
    public ApiResponse<Role> assignPermissions(@PathVariable UUID id, @Valid @RequestBody Map<String, Set<UUID>> body) {
        return ApiResponse.ok("Permissions assigned", roleService.assignPermissions(id, body.get("permissionIds")));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLES:DELETE')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        roleService.delete(id);
        return ApiResponse.deleted("Role deleted");
    }
}
