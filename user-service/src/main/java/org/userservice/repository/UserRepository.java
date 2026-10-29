package org.userservice.repository;

import org.userservice.model.RoleName;
import org.userservice.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmailOrUsername(String email, String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    Page<User> findByTenantId(String tenantId, Pageable pageable);

    Page<User> findByTenantIdAndActiveTrue(String tenantId, Pageable pageable);

    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :loginAt WHERE u.id = :id")
    void updateLastLogin(@Param("id") UUID id, @Param("loginAt") LocalDateTime loginAt);

    @Modifying
    @Query("UPDATE User u SET u.active = :active WHERE u.id = :id")
    int setActiveStatus(@Param("id") UUID id, @Param("active") boolean active);

    long countByTenantIdAndActiveTrue(String tenantId);

    /** Count how many users have been assigned a specific role (used to generate admin{N} usernames). */
    @Query("SELECT COUNT(DISTINCT u) FROM User u JOIN u.roles r WHERE r.name = :roleName")
    long countByRoleName(@Param("roleName") RoleName roleName);
}
