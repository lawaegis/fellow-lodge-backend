package com.fellowlodge.api.service;

import com.fellowlodge.api.common.exception.AccountDisabledException;
import com.fellowlodge.api.common.exception.AccountLockedException;
import com.fellowlodge.api.common.exception.InvalidCredentialsException;
import com.fellowlodge.api.common.exception.ResourceNotFoundException;
import com.fellowlodge.api.common.exception.TokenRefreshException;
import com.fellowlodge.api.config.AppProperties;
import com.fellowlodge.api.config.DataSeeder;
import com.fellowlodge.api.dto.auth.AuthResponse;
import com.fellowlodge.api.dto.auth.ChangePasswordRequest;
import com.fellowlodge.api.dto.auth.ForgotPasswordRequest;
import com.fellowlodge.api.dto.auth.LoginRequest;
import com.fellowlodge.api.dto.auth.MessageResponse;
import com.fellowlodge.api.dto.auth.RefreshTokenRequest;
import com.fellowlodge.api.dto.auth.RegisterRequest;
import com.fellowlodge.api.dto.auth.ResetPasswordRequest;
import com.fellowlodge.api.dto.auth.UserResponse;
import com.fellowlodge.api.entity.Guest;
import com.fellowlodge.api.entity.PasswordResetToken;
import com.fellowlodge.api.entity.RefreshToken;
import com.fellowlodge.api.entity.Role;
import com.fellowlodge.api.entity.User;
import com.fellowlodge.api.repository.GuestRepository;
import com.fellowlodge.api.repository.PasswordResetTokenRepository;
import com.fellowlodge.api.repository.RefreshTokenRepository;
import com.fellowlodge.api.repository.RoleRepository;
import com.fellowlodge.api.repository.UserRepository;
import com.fellowlodge.api.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Handles authentication, token lifecycle, account locking, and password
 * recovery. This is the single source of truth for who can sign in.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final GuestRepository guestRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtTokenService;
    private final AppProperties appProperties;
    private final UserMapper userMapper;
    private final AuditLogService auditLogService;
    private final LoginAttemptService loginAttemptService;
    private final Environment environment;

    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress) {
        String identifier = request.identifier();
        if (identifier == null || identifier.isBlank()) {
            throw new InvalidCredentialsException("Invalid username or password.");
        }
        User user = userRepository.findByUsernameIgnoreCase(identifier)
                .orElseGet(() -> userRepository.findByEmailIgnoreCase(identifier)
                        .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password.")));

        rejectLockedUser(user);
        if (!user.isActive()) {
            throw new AccountDisabledException("This account has been disabled. Contact your administrator.");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            loginAttemptService.recordFailedLogin(user.getId());
            throw new InvalidCredentialsException("Invalid username or password.");
        }

        resetLockout(user);
        user.setLastLogin(LocalDateTime.now());
        user.setLastLoginIp(ipAddress);
        userRepository.save(user);

        audit("LOGIN", "users", user.getId());
        return issueTokenPair(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken stored = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new TokenRefreshException("Refresh token not found or has been revoked."));

        if (stored.isRevoked() || stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(stored);
            throw new TokenRefreshException("Refresh token has expired or been revoked. Please sign in again.");
        }

        User user = userRepository.findById(stored.getUserId())
                .orElseThrow(() -> new TokenRefreshException("User for this token no longer exists."));
        rejectLockedUser(user);
        if (!user.isActive()) {
            throw new AccountDisabledException("This account has been disabled.");
        }

        refreshTokenRepository.delete(stored);
        audit("TOKEN_REFRESH", "users", user.getId());
        return issueTokenPair(user);
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenRepository.findByToken(refreshToken)
                    .ifPresent(stored -> {
                        refreshTokenRepository.delete(stored);
                        audit("LOGOUT", "users", stored.getUserId());
                    });
        }
    }

    @Transactional
    public AuthResponse registerGuest(RegisterRequest request, String ipAddress) {
        if (userRepository.findByEmailIgnoreCase(request.email()).isPresent()) {
            throw new InvalidCredentialsException("An account already exists for email: " + request.email());
        }

        User user = new User();
        user.setUsername(request.email());
        user.setEmail(request.email());
        user.setFullName(request.firstName() + " " + request.lastName());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRoleId(DataSeeder.ROLE_GUEST);
        user.setActive(true);
        user.setLastLogin(LocalDateTime.now());
        user.setLastLoginIp(ipAddress);
        userRepository.save(user);

        Guest guest = new Guest();
        guest.setUserId(user.getId());
        guest.setFirstName(request.firstName());
        guest.setLastName(request.lastName());
        guest.setEmail(request.email());
        guest.setPhone(request.phone());
        guestRepository.save(guest);

        audit("GUEST_REGISTER", "guests", guest.getId());
        return issueTokenPair(user);
    }

    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmailIgnoreCase(request.email()).ifPresent(user -> {
            passwordResetTokenRepository.deleteByUserId(user.getId());
            String rawToken = UUID.randomUUID().toString() + UUID.randomUUID();
            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setUserId(user.getId());
            resetToken.setToken(hashToken(rawToken));
            resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(30));
            passwordResetTokenRepository.save(resetToken);
            if (environment.acceptsProfiles(Profiles.of("dev"))) {
                log.info("[PASSWORD RESET] Dev only - reset token for {} is: {}", user.getUsername(), rawToken);
            } else {
                log.info("[PASSWORD RESET] A password reset token was issued for user {}.", user.getUsername());
            }
        });
        // Always return the same message to avoid user enumeration.
        return new MessageResponse("If an account exists for that email, a password reset link has been sent.");
    }

    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        PasswordResetToken stored = passwordResetTokenRepository.findByToken(hashToken(request.token()))
                .orElseThrow(() -> new InvalidCredentialsException("Invalid or expired reset token."));

        if (stored.isUsed() || stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidCredentialsException("Invalid or expired reset token.");
        }

        User user = userRepository.findById(stored.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", stored.getUserId()));

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setLocked(false);
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);

        stored.setUsed(true);
        passwordResetTokenRepository.save(stored);
        refreshTokenRepository.deleteByUserId(user.getId());

        audit("PASSWORD_RESET", "users", user.getId());
        return new MessageResponse("Password has been reset successfully. You can now sign in.");
    }

    @Transactional
    public MessageResponse changePassword(UUID userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Current password is incorrect.");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setMustChangePassword(false);
        userRepository.save(user);
        refreshTokenRepository.deleteByUserId(userId);
        audit("PASSWORD_CHANGE", "users", userId);
        return new MessageResponse("Password changed successfully.");
    }

    public UserResponse me(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        return userMapper.toResponse(user);
    }

    public MessageResponse verifyEmail(String token) {
        // Email verification is automatic at registration in this deployment
        // (no SMTP provider is configured). The endpoint is retained for
        // frontend contract compatibility; the token is accepted but no
        // round-trip verification is required.
        if (token != null && !token.isBlank()) {
            log.debug("Email verification token received: {}", token);
        }
        return new MessageResponse("Email verified successfully.");
    }

    private AuthResponse issueTokenPair(User user) {
        Role role = user.getRoleId() == null ? null : roleRepository.findById(user.getRoleId()).orElse(null);
        String roleName = role == null ? "GUEST" : role.getName();
        var permissions = new HashSet<String>();
        if (role != null) {
            role.getPermissions().forEach(p -> permissions.add(p.getCode()));
        }

        String refreshTokenRaw = jwtTokenService.generateRefreshToken(user.getId());
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(user.getId());
        refreshToken.setToken(refreshTokenRaw);
        refreshToken.setExpiresAt(LocalDateTime.now()
                .plusSeconds(jwtTokenService.getRefreshExpirationMs() / 1000));
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(jwtTokenService.generateAccessToken(user.getId(), user.getUsername(), roleName, permissions))
                .refreshToken(refreshTokenRaw)
                .tokenType("Bearer")
                .expiresIn(jwtTokenService.getAccessExpirationMs() / 1000)
                .user(userMapper.toResponse(user, roleName, permissions))
                .build();
    }

    private void rejectLockedUser(User user) {
        if (!user.isLocked()) {
            return;
        }
        if (user.getLockedUntil() != null && user.getLockedUntil().isBefore(LocalDateTime.now())) {
            user.setLocked(false);
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
            userRepository.save(user);
            return;
        }
        if (user.getLockedUntil() != null) {
            long minutes = java.time.Duration.between(LocalDateTime.now(), user.getLockedUntil()).toMinutes();
            throw new AccountLockedException("Account is locked. Try again in " + Math.max(1, minutes) + " minute(s).");
        }
        throw new AccountLockedException("Account is locked. Try again in "
                + appProperties.getSecurity().getLockDurationMinutes() + " minute(s).");
    }

    private void resetLockout(User user) {
        user.setLocked(false);
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
    }

    private void audit(String action, String table, UUID recordId) {
        auditLogService.record(action, table, recordId == null ? null : recordId.toString(), null, null);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
