package com.fellowlodge.api.service;

import com.fellowlodge.api.common.exception.DuplicateResourceException;
import com.fellowlodge.api.common.exception.InvalidOperationException;
import com.fellowlodge.api.common.exception.ResourceNotFoundException;
import com.fellowlodge.api.entity.User;
import com.fellowlodge.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public Page<User> findAll(int page, int size, String sort, String search, UUID roleId, Boolean active) {
        Pageable pageable = PageRequest.of(page, size, buildSort(sort));
        Specification<User> spec = Specification.where(null);
        if (StringUtils.hasText(search)) {
            String q = search.toLowerCase();
            spec = spec.and((root, cq, cb) -> cb.or(
                    cb.like(cb.lower(root.get("username")), "%" + q + "%"),
                    cb.like(cb.lower(root.get("fullName")), "%" + q + "%"),
                    cb.like(cb.lower(root.get("email")), "%" + q + "%")));
        }
        if (roleId != null) {
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("roleId"), roleId));
        }
        if (active != null) {
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("active"), active));
        }
        return userRepository.findAll(spec, pageable);
    }

    public List<User> findAll() {
        return userRepository.findAll(Sort.by(Sort.Direction.ASC, "fullName"));
    }

    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User with email " + email));
    }

    public long countActive() {
        return userRepository.countByActiveTrue();
    }

    public long countLocked() {
        return userRepository.countByLockedTrue();
    }

    @Transactional
    public User create(User user, String rawPassword) {
        if (userRepository.findByEmailIgnoreCase(user.getEmail()).isPresent()) {
            throw new DuplicateResourceException("A user already exists for email: " + user.getEmail());
        }
        if (StringUtils.hasText(user.getUsername())
                && userRepository.findByUsernameIgnoreCase(user.getUsername()).isPresent()) {
            throw new DuplicateResourceException("Username is already taken: " + user.getUsername());
        }
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setActive(true);
        user.setLocked(false);
        User saved = userRepository.save(user);
        auditLogService.record("USER_CREATE", "users", saved.getId().toString(), null, null);
        return saved;
    }

    @Transactional
    public User update(UUID id, User updated) {
        User user = findById(id);
        if (updated.getEmail() != null && !updated.getEmail().equalsIgnoreCase(user.getEmail())
                && userRepository.findByEmailIgnoreCase(updated.getEmail()).isPresent()) {
            throw new DuplicateResourceException("A user already exists for email: " + updated.getEmail());
        }
        user.setEmail(updated.getEmail());
        user.setFullName(updated.getFullName());
        user.setRoleId(updated.getRoleId());
        user.setAvatarUrl(updated.getAvatarUrl());
        user.setActive(updated.isActive());
        User saved = userRepository.save(user);
        auditLogService.record("USER_UPDATE", "users", saved.getId().toString(), null, null);
        return saved;
    }

    @Transactional
    public void delete(UUID id) {
        User user = findById(id);
        userRepository.delete(user);
        auditLogService.record("USER_DELETE", "users", id.toString(), null, null);
    }

    @Transactional
    public User setActive(UUID id, boolean active) {
        User user = findById(id);
        user.setActive(active);
        return userRepository.save(user);
    }

    @Transactional
    public User lock(UUID id) {
        User user = findById(id);
        user.setLocked(true);
        return userRepository.save(user);
    }

    @Transactional
    public User unlock(UUID id) {
        User user = findById(id);
        user.setLocked(false);
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        return userRepository.save(user);
    }

    @Transactional
    public User resetPassword(UUID id, String newPassword) {
        if (!StringUtils.hasText(newPassword)) {
            throw new InvalidOperationException("New password is required.");
        }
        User user = findById(id);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setLocked(false);
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        User saved = userRepository.save(user);
        auditLogService.record("PASSWORD_RESET_BY_ADMIN", "users", id.toString(), null, null);
        return saved;
    }

    private Sort buildSort(String sort) {
        if (!StringUtils.hasText(sort)) {
            return Sort.by(Sort.Direction.ASC, "fullName");
        }
        String[] parts = sort.split(",");
        Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1])
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, parts[0]);
    }
}
