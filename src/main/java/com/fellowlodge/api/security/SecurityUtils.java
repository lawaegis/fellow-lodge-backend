package com.fellowlodge.api.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Helpers to access the current authenticated principal.
 */
@Component
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static UUID currentUserId() {
        AuthUser user = currentUser();
        return user == null ? null : user.getId();
    }

    public static String currentUsername() {
        AuthUser user = currentUser();
        return user == null ? null : user.getUsername();
    }

    public static String currentRole() {
        AuthUser user = currentUser();
        return user == null ? null : user.getRole();
    }

    public static boolean hasPermission(String permission) {
        AuthUser user = currentUser();
        return user != null && user.getPermissions().contains(permission);
    }

    public static AuthUser currentUser() {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            return null;
        }
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof AuthUser authUser) {
            return authUser;
        }
        return null;
    }
}
