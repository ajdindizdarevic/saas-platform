package org.tenantservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tenantservice.exception.TenantNotFoundException;
import org.tenantservice.model.Tenant;
import org.tenantservice.model.TenantStatus;
import org.tenantservice.repository.TenantRepository;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;

    public Page<Tenant> getAllTenants(Pageable pageable) {
        return tenantRepository.findAll(pageable);
    }

    public Page<Tenant> getTenantsByStatus(TenantStatus status, Pageable pageable) {
        return tenantRepository.findByStatus(status, pageable);
    }

    public Tenant getTenantById(UUID id) {
        return tenantRepository.findById(id)
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found: " + id));
    }

    @Transactional
    public Tenant suspendTenant(UUID id) {
        Tenant tenant = getTenantById(id);
        tenant.setStatus(TenantStatus.SUSPENDED);
        log.info("Tenant {} suspended", id);
        return tenantRepository.save(tenant);
    }

    @Transactional
    public Tenant activateTenant(UUID id) {
        Tenant tenant = getTenantById(id);
        tenant.setStatus(TenantStatus.ACTIVE);
        log.info("Tenant {} activated", id);
        return tenantRepository.save(tenant);
    }
}