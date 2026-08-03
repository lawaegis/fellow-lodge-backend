package com.fellowlodge.api.service;

import com.fellowlodge.api.common.exception.InvalidOperationException;
import com.fellowlodge.api.common.exception.ResourceNotFoundException;
import com.fellowlodge.api.entity.Permission;
import com.fellowlodge.api.entity.Role;
import com.fellowlodge.api.repository.PermissionRepository;
import com.fellowlodge.api.repository.RoleRepository;
import com.fellowlodge.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;

    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    public List<Permission> findAllPermissions() {
        return permissionRepository.findAll();
    }

    public Role findById(UUID id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", id));
    }

    @Transactional
    public Role create(Role role) {
        if (roleRepository.existsByNameIgnoreCase(role.getName())) {
            throw new InvalidOperationException("A role already exists with name: " + role.getName());
        }
        return roleRepository.save(role);
    }

    @Transactional
    public Role update(UUID id, Role updated) {
        Role role = findById(id);
        role.setName(updated.getName());
        role.setDescription(updated.getDescription());
        role.setSystem(updated.isSystem());
        return roleRepository.save(role);
    }

    @Transactional
    public void delete(UUID id) {
        Role role = findById(id);
        if (role.isSystem()) {
            throw new InvalidOperationException("System roles cannot be deleted.");
        }
        if (!userRepository.findByRoleId(id).isEmpty()) {
            throw new InvalidOperationException("Role is assigned to users and cannot be deleted.");
        }
        roleRepository.delete(role);
    }

    @Transactional
    public Role assignPermissions(UUID roleId, Set<UUID> permissionIds) {
        Role role = findById(roleId);
        Set<Permission> permissions = new HashSet<>();
        if (permissionIds != null) {
            for (UUID permissionId : permissionIds) {
                permissions.add(permissionRepository.findById(permissionId)
                        .orElseThrow(() -> new ResourceNotFoundException("Permission", permissionId)));
            }
        }
        role.setPermissions(permissions);
        return roleRepository.save(role);
    }
}
