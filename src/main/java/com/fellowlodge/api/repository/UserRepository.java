package com.fellowlodge.api.repository;

import com.fellowlodge.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByUsernameIgnoreCase(String username);

    List<User> findByRoleId(UUID roleId);

    long countByActiveTrue();

    long countByLockedTrue();

    @Query("select u.roleId from User u where u.id = :id")
    Optional<UUID> findRoleIdById(@Param("id") UUID id);
}
