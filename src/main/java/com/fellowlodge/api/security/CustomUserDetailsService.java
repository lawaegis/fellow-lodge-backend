package com.fellowlodge.api.security;

import com.fellowlodge.api.entity.Role;
import com.fellowlodge.api.entity.User;
import com.fellowlodge.api.repository.RoleRepository;
import com.fellowlodge.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Loads the authenticated principal by username or email and resolves its
 * role-based permission set. Rejects accounts that are disabled or locked so a
 * previously issued token cannot outlive an administrative status change.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameIgnoreCase(usernameOrEmail)
                .orElseGet(() -> userRepository.findByEmailIgnoreCase(usernameOrEmail)
                        .orElseThrow(() -> new UsernameNotFoundException(
                                "No user found with username or email: " + usernameOrEmail)));

        if (!user.isActive()) {
            throw new DisabledException("This account has been disabled.");
        }
        if (user.isLocked() && (user.getLockedUntil() == null
                || user.getLockedUntil().isAfter(LocalDateTime.now()))) {
            throw new LockedException("This account is locked.");
        }

        String roleName = "GUEST";
        Set<String> permissions = Set.of();
        if (user.getRoleId() != null) {
            Role role = roleRepository.findById(user.getRoleId()).orElse(null);
            if (role != null) {
                roleName = role.getName();
                permissions = role.getPermissions().stream()
                        .map(p -> p.getCode())
                        .collect(Collectors.toSet());
            }
        }
        return AuthUser.from(user, roleName, permissions);
    }
}
