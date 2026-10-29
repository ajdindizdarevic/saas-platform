package org.userservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.userservice.dto.request.ChangePasswordDto;
import org.userservice.dto.request.CreateUserDto;
import org.userservice.dto.response.UserResponseDto;
import org.userservice.exception.UserNotFoundException;
import org.userservice.model.Role;
import org.userservice.model.RoleName;
import org.userservice.model.User;
import org.userservice.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository   userRepository;
    private final PasswordService  passwordService;
    private final RoleResolverService roleResolverService;

    // ── create ────────────────────────────────────────────────────────────────

    @Transactional
    public UserResponseDto createUser(CreateUserDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already in use: " + dto.getEmail());
        }
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Username already taken: " + dto.getUsername());
        }

        Set<RoleName> roleNames = (dto.getRoles() == null || dto.getRoles().isEmpty())
                ? Set.of(RoleName.USER)
                : dto.getRoles();

        Set<Role> roles = roleResolverService.resolveRoles(roleNames);

        User user = User.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .username(dto.getUsername())
                .password(passwordService.encode(dto.getPassword()))
                .phoneNumber(dto.getPhoneNumber())
                .tenantId(dto.getTenantId())
                .tenantSchema(dto.getTenantSchema())
                .onboardingRequestId(dto.getOnboardingRequestId())
                .roles(roles)
                .active(true)
                .emailVerified(false)
                .build();

        User saved = userRepository.save(user);
        log.info("Created user id={} for tenant={}", saved.getId(), saved.getTenantId());
        return toDto(saved);
    }

    // ── read ──────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public UserResponseDto getUserById(UUID id) {
        return toDto(findById(id));
    }

    @Transactional(readOnly = true)
    public UserResponseDto getUserByEmail(String email) {
        return toDto(userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + email)));
    }

    @Transactional(readOnly = true)
    public Page<UserResponseDto> getUsersByTenant(String tenantId, Pageable pageable) {
        return userRepository.findByTenantIdAndActiveTrue(tenantId, pageable).map(this::toDto);
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Transactional
    public void changePassword(UUID userId, ChangePasswordDto dto) {
        User user = findById(userId);
        if (!passwordService.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        if (!passwordService.isStrong(dto.getNewPassword())) {
            throw new IllegalArgumentException("New password must be at least 8 characters with letters and digits");
        }
        user.setPassword(passwordService.encode(dto.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed for user id={}", userId);
    }

    @Transactional
    public UserResponseDto setActiveStatus(UUID id, boolean active) {
        int updated = userRepository.setActiveStatus(id, active);
        if (updated == 0) throw new UserNotFoundException("User not found: " + id);
        return toDto(findById(id));
    }

    // ── internal helpers ──────────────────────────────────────────────────────

    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + id));
    }

    private UserResponseDto toDto(User u) {
        return UserResponseDto.builder()
                .id(u.getId())
                .firstName(u.getFirstName())
                .lastName(u.getLastName())
                .fullName(u.getFullName())
                .email(u.getEmail())
                .username(u.getUsername())
                .phoneNumber(u.getPhoneNumber())
                .tenantId(u.getTenantId())
                .tenantSchema(u.getTenantSchema())
                .roles(u.getRoles().stream()
                        .map(r -> r.getName().name())
                        .collect(Collectors.toSet()))
                .active(u.isActive())
                .emailVerified(u.isEmailVerified())
                .profilePictureUrl(u.getProfilePictureUrl())
                .lastLoginAt(u.getLastLoginAt())
                .createdAt(u.getCreatedAt())
                .updatedAt(u.getUpdatedAt())
                .build();
    }
}