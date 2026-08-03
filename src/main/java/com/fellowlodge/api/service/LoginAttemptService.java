package com.fellowlodge.api.service;

import com.fellowlodge.api.config.AppProperties;
import com.fellowlodge.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Records failed login attempts in their own transaction so the attempt count
 * survives the rollback of the outer login transaction (which throws an
 * authentication exception).
 */
@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final AppProperties appProperties;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailedLogin(UUID userId) {
        userRepository.findById(userId).ifPresent(user -> {
            int maxAttempts = appProperties.getSecurity().getMaxLoginAttempts();
            int attempts = user.getFailedLoginAttempts() + 1;
            if (attempts >= maxAttempts) {
                user.setLocked(true);
                user.setLockedUntil(LocalDateTime.now()
                        .plusMinutes(appProperties.getSecurity().getLockDurationMinutes()));
                user.setFailedLoginAttempts(0);
                auditLogService.record("ACCOUNT_LOCKED", "users", user.getId().toString(), null, null);
            } else {
                user.setFailedLoginAttempts(attempts);
            }
            userRepository.save(user);
        });
    }
}
