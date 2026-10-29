package org.tenantservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tenantservice.dto.response.ApiResponse;
import org.tenantservice.dto.response.TenantResponseDto;
import org.tenantservice.model.Tenant;
import org.tenantservice.model.TenantStatus;
import org.tenantservice.service.TenantService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<TenantResponseDto>>> getAllTenants(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Page<TenantResponseDto> page = tenantService.getAllTenants(pageable).map(this::toDto);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TenantResponseDto>> getTenantById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(toDto(tenantService.getTenantById(id))));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<Page<TenantResponseDto>>> getTenantsByStatus(
            @PathVariable TenantStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<TenantResponseDto> page = tenantService.getTenantsByStatus(status, pageable).map(this::toDto);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @PatchMapping("/{id}/suspend")
    public ResponseEntity<ApiResponse<TenantResponseDto>> suspendTenant(@PathVariable UUID id) {
        return ResponseEntity.ok(
                ApiResponse.success("Tenant suspended", toDto(tenantService.suspendTenant(id))));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<TenantResponseDto>> activateTenant(@PathVariable UUID id) {
        return ResponseEntity.ok(
                ApiResponse.success("Tenant activated", toDto(tenantService.activateTenant(id))));
    }

    // ── mapper ────────────────────────────────────────────────────────────────

    private TenantResponseDto toDto(Tenant t) {
        return TenantResponseDto.builder()
                .id(t.getId())
                .companyName(t.getCompanyName())
                .schemaName(t.getSchemaName())
                .contactEmail(t.getContactEmail())
                .contactName(t.getContactName())
                .phoneNumber(t.getPhoneNumber())
                .businessType(t.getBusinessType())
                .expectedUsers(t.getExpectedUsers())
                .maxUsers(t.getMaxUsers())
                .maxStorageGb(t.getMaxStorageGb())
                .plan(t.getPlan())
                .status(t.getStatus())
                .onboardingRequestId(t.getOnboardingRequestId())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}