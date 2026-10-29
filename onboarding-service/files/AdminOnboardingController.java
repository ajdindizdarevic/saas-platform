package org.saas.onboarding.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.saas.onboarding.dto.request.ApproveRequestDto;
import org.saas.onboarding.dto.response.ApiResponse;
import org.saas.onboarding.dto.response.OnboardingResponseDto;
import org.saas.onboarding.service.AdminApprovalService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/onboarding")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Onboarding", description = "Admin operations for onboarding request management")
public class AdminOnboardingController {

    private final AdminApprovalService adminApprovalService;

    @PutMapping("/{id}/review")
    @Operation(summary = "Process approval or rejection of an onboarding request")
    public ResponseEntity<ApiResponse<OnboardingResponseDto>> processApproval(
            @PathVariable UUID id,
            @Valid @RequestBody ApproveRequestDto dto) {
        OnboardingResponseDto response = adminApprovalService.processApproval(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Request processed successfully", response));
    }

    @PatchMapping("/{id}/under-review")
    @Operation(summary = "Mark an onboarding request as under review")
    public ResponseEntity<ApiResponse<OnboardingResponseDto>> markUnderReview(
            @PathVariable UUID id,
            Authentication authentication) {
        String reviewedBy = authentication.getName();
        OnboardingResponseDto response = adminApprovalService.markUnderReview(id, reviewedBy);
        return ResponseEntity.ok(ApiResponse.success("Request marked as under review", response));
    }
}
