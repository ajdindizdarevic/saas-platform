package org.userservice.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.userservice.dto.request.ChangePasswordDto;
import org.userservice.dto.request.CreateUserDto;
import org.userservice.dto.response.ApiResponse;
import org.userservice.dto.response.UserResponseDto;
import org.userservice.security.UserPrincipal;
import org.userservice.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management within a tenant")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get the currently authenticated user's profile")
    public ResponseEntity<ApiResponse<UserResponseDto>> getMe(
            @AuthenticationPrincipal UserPrincipal principal) {
        UserResponseDto user = userService.getUserById(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PatchMapping("/me/password")
    @Operation(summary = "Change the authenticated user's own password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ChangePasswordDto dto) {
        userService.changePassword(principal.getId(), dto);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a user by ID")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserById(id)));
    }

    @GetMapping
    @Operation(summary = "List all active users for the current tenant")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<Page<UserResponseDto>>> getUsersByTenant(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Page<UserResponseDto> page =
                userService.getUsersByTenant(principal.getTenantId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @PostMapping
    @Operation(summary = "Create a user (admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponseDto>> createUser(
            @Valid @RequestBody CreateUserDto dto) {
        UserResponseDto created = userService.createUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created", created));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate a user (admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponseDto>> deactivate(@PathVariable UUID id) {
        return ResponseEntity.ok(
                ApiResponse.success("User deactivated", userService.setActiveStatus(id, false)));
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Re-activate a user (admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponseDto>> activate(@PathVariable UUID id) {
        return ResponseEntity.ok(
                ApiResponse.success("User activated", userService.setActiveStatus(id, true)));
    }
}