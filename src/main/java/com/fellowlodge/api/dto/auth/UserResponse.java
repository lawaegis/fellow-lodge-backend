package com.fellowlodge.api.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private UUID id;
    private String username;
    private String email;
    private String fullName;
    private String role;
    private Set<String> permissions;
    private boolean active;
    private boolean locked;
    private boolean mustChangePassword;
    private String avatarUrl;
    private java.time.LocalDateTime lastLogin;
    private java.time.LocalDateTime createdAt;
}
