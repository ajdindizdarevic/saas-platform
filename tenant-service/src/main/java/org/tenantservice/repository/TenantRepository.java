package org.tenantservice.repository;


import org.tenantservice.model.Tenant;
import org.tenantservice.model.TenantStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID> {

    Optional<Tenant> findBySchemaName(String schemaName);

    Optional<Tenant> findByContactEmail(String contactEmail);

    boolean existsBySchemaName(String schemaName);

    boolean existsByContactEmail(String contactEmail);

    Page<Tenant> findByStatus(TenantStatus status, Pageable pageable);

    Optional<Tenant> findByOnboardingRequestId(UUID onboardingRequestId);

    @Modifying
    @Query("UPDATE Tenant t SET t.status = :status WHERE t.id = :id")
    int updateStatus(@Param("id") UUID id, @Param("status") TenantStatus status);
}