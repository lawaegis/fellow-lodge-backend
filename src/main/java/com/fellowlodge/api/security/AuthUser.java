package com.fellowlodge.api.security;

import com.fellowlodge.api.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Authenticated principal stored in the SecurityContext.
 */
@Getter
public class AuthUser implements UserDetails {

    private final UUID id;
    private final String username;
    private final String password;
    private final String role;
    private final Set<String> permissions;
    private final boolean active;
    private final boolean locked;
    private final boolean mustChangePassword;

    public AuthUser(UUID id, String username, String password, String role,
                    Set<String> permissions, boolean active, boolean locked, boolean mustChangePassword) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.permissions = permissions == null ? Set.of() : permissions;
        this.active = active;
        this.locked = locked;
        this.mustChangePassword = mustChangePassword;
    }

    public static AuthUser from(User user, String roleName, Set<String> permissions) {
        return new AuthUser(
                user.getId(),
                user.getUsername(),
                user.getPasswordHash(),
                roleName,
                permissions,
                user.isActive(),
                user.isLocked(),
                user.isMustChangePassword());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !locked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
