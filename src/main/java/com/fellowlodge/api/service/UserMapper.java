package com.fellowlodge.api.service;

import com.fellowlodge.api.dto.auth.UserResponse;
import com.fellowlodge.api.entity.Role;
import com.fellowlodge.api.entity.User;
import com.fellowlodge.api.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;

/**
 * Builds user response DTOs with resolved role name and permission codes.
 */
@Component
@RequiredArgsConstructor
public class UserMapper {

    private final RoleRepository roleRepository;

    public UserResponse toResponse(User user) {
        String roleName = "GUEST";
        var permissions = new HashSet<String>();
        if (user.getRoleId() != null) {
            Role role = roleRepository.findById(user.getRoleId()).orElse(null);
            if (role != null) {
                roleName = role.getName();
                role.getPermissions().forEach(p -> permissions.add(p.getCode()));
            }
        }
        return toResponse(user, roleName, permissions);
    }

    public UserResponse toResponse(User user, String roleName, java.util.Set<String> permissions) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(roleName)
                .permissions(permissions)
                .active(user.isActive())
                .locked(user.isLocked())
                .mustChangePassword(user.isMustChangePassword())
                .avatarUrl(user.getAvatarUrl())
                .lastLogin(user.getLastLogin())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
