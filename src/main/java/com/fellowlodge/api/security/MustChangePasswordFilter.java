package com.fellowlodge.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Blocks all authenticated endpoints (except password change and profile
 * introspection) until a user flagged {@code mustChangePassword} has set a new
 * password. Default-seeded accounts carry this flag so predictable starter
 * credentials cannot be used in production.
 */
@Component
public class MustChangePasswordFilter extends OncePerRequestFilter {

    private static final String[] ALLOWED_PATHS = {
            "/api/auth/change-password",
            "/api/auth/me",
            "/api/auth/logout",
            "/api/auth/refresh"
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthUser user
                && user.isMustChangePassword()) {
            if (!isAllowed(request)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                response.getWriter().write(
                        "{\"timestamp\":\"" + java.time.Instant.now() + "\","
                        + "\"status\":403,\"success\":false,"
                        + "\"message\":\"You must change your password before continuing.\","
                        + "\"code\":\"PASSWORD_CHANGE_REQUIRED\"}");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean isAllowed(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri == null) return false;
        for (String path : ALLOWED_PATHS) {
            if (uri.equals(path)) return true;
        }
        return false;
    }
}
