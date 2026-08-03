package com.fellowlodge.api.controller;

import com.fellowlodge.api.common.ApiResponse;
import com.fellowlodge.api.common.PageResponse;
import com.fellowlodge.api.dto.user.CreateUserRequest;
import com.fellowlodge.api.entity.User;
import com.fellowlodge.api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAuthority('USERS:READ')")
    public ApiResponse<List<User>> findAll(@RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "20") int size,
                                           @RequestParam(required = false) String sort,
                                           @RequestParam(required = false) String search,
                                           @RequestParam(required = false) UUID roleId,
                                           @RequestParam(required = false) Boolean active) {
        Page<User> result = userService.findAll(page, size, sort, search, roleId, active);
        return ApiResponse.ok(result.getContent(), PageResponse.from(result));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('USERS:READ')")
    public ApiResponse<List<User>> findAll() {
        return ApiResponse.ok(userService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USERS:READ')")
    public ApiResponse<User> findById(@PathVariable UUID id) {
        return ApiResponse.ok(userService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USERS:WRITE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<User> create(@Valid @RequestBody CreateUserRequest request) {
        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setFullName(request.fullName());
        user.setRoleId(request.roleId());
        return ApiResponse.created("User created", userService.create(user, request.password()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USERS:WRITE')")
    public ApiResponse<User> update(@PathVariable UUID id, @Valid @RequestBody User user) {
        return ApiResponse.ok("User updated", userService.update(id, user));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('USERS:WRITE')")
    public ApiResponse<User> setActive(@PathVariable UUID id, @RequestParam boolean active) {
        return ApiResponse.ok("User status updated", userService.setActive(id, active));
    }

    @PostMapping("/{id}/lock")
    @PreAuthorize("hasAuthority('USERS:WRITE')")
    public ApiResponse<User> lock(@PathVariable UUID id) {
        return ApiResponse.ok("User locked", userService.lock(id));
    }

    @PostMapping("/{id}/unlock")
    @PreAuthorize("hasAuthority('USERS:WRITE')")
    public ApiResponse<User> unlock(@PathVariable UUID id) {
        return ApiResponse.ok("User unlocked", userService.unlock(id));
    }

    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasAuthority('USERS:WRITE')")
    public ApiResponse<User> resetPassword(@PathVariable UUID id, @RequestParam String password) {
        return ApiResponse.ok("Password reset", userService.resetPassword(id, password));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USERS:DELETE')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        userService.delete(id);
        return ApiResponse.deleted("User deleted");
    }
}
