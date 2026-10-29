package org.onboardingservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.onboardingservice.dto.request.CreateOnboardingRequestDto;
import org.onboardingservice.dto.response.ApiResponse;
import org.onboardingservice.dto.response.OnboardingResponseDto;
import org.onboardingservice.model.RequestStatus;
import org.onboardingservice.service.OnboardingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/onboarding")
@RequiredArgsConstructor
@Tag(name = "Onboarding", description = "Tenant onboarding request management")
public class OnboardingRequestController {

    private final OnboardingService onboardingService;

    @PostMapping
    @Operation(summary = "Submit a new onboarding request")
    public ResponseEntity<ApiResponse<OnboardingResponseDto>> createRequest(
            @Valid @RequestBody CreateOnboardingRequestDto dto) {
        OnboardingResponseDto response = onboardingService.createRequest(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Onboarding request submitted successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get onboarding request by ID")
    public ResponseEntity<ApiResponse<OnboardingResponseDto>> getRequest(@PathVariable UUID id) {
        OnboardingResponseDto response = onboardingService.getRequestById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "List all onboarding requests")
    public ResponseEntity<ApiResponse<Page<OnboardingResponseDto>>> getAllRequests(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Page<OnboardingResponseDto> page = onboardingService.getAllRequests(pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get onboarding requests by status")
    public ResponseEntity<ApiResponse<Page<OnboardingResponseDto>>> getByStatus(
            @PathVariable RequestStatus status,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Page<OnboardingResponseDto> page = onboardingService.getRequestsByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel a pending onboarding request")
    public ResponseEntity<ApiResponse<OnboardingResponseDto>> cancelRequest(@PathVariable UUID id) {
        OnboardingResponseDto response = onboardingService.cancelRequest(id);
        return ResponseEntity.ok(ApiResponse.success("Request cancelled", response));
    }
}