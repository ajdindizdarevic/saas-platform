package org.userservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.userservice.dto.request.LoginRequestDto;
import org.userservice.dto.request.RegisterRequestDto;
import org.userservice.dto.request.CreateUserDto;
import org.userservice.dto.response.LoginResponseDto;
import org.userservice.dto.response.UserResponseDto;
import org.userservice.exception.UnauthorizedException;
import org.userservice.messaging.event.UserRegisteredEvent;
import org.userservice.messaging.producer.UserRegisteredProducer;
import org.userservice.model.User;
import org.userservice.repository.UserRepository;
import org.userservice.security.UserPrincipal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager  authenticationManager;
    private final JwtService             jwtService;
    private final UserService            userService;
    private final UserRepository         userRepository;
    private final UserRegisteredProducer userRegisteredProducer;

    // ── login ─────────────────────────────────────────────────────────────────

    @Transactional
    public LoginResponseDto login(LoginRequestDto dto) {
        Authentication auth;
        try {
            auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            dto.getUsernameOrEmail(), dto.getPassword()));
        } catch (BadCredentialsException ex) {
            log.warn("Failed login attempt for identifier: {}", dto.getUsernameOrEmail());
            throw new UnauthorizedException("Invalid credentials");
        }

        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();

        // Update last-login timestamp
        userRepository.updateLastLogin(principal.getId(), LocalDateTime.now());

        String accessToken  = jwtService.generateAccessToken(principal);
        String refreshToken = jwtService.generateRefreshToken(principal);

        User user = userService.findById(principal.getId());

        log.info("User {} logged in from tenant={}", principal.getUsername(), principal.getTenantId());

        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpiration())
                .tenantId(principal.getTenantId())
                .tenantSchema(principal.getTenantSchema())
                .roles(principal.getRoleNames())
                .user(userService.getUserById(principal.getId()))
                .build();
    }

    // ── self-registration ─────────────────────────────────────────────────────

    @Transactional
    public UserResponseDto register(RegisterRequestDto dto) {
        CreateUserDto createDto = CreateUserDto.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .username(dto.getUsername())
                .password(dto.getPassword())
                .phoneNumber(dto.getPhoneNumber())
                .tenantId(dto.getTenantId())
                .roles(Set.of(org.userservice.model.RoleName.USER))
                .build();

        UserResponseDto created = userService.createUser(createDto);

        // Publish event so onboarding-service creates a queue entry for this user.
        userRegisteredProducer.publish(UserRegisteredEvent.builder()
                .userId(created.getId() != null ? created.getId().toString() : null)
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .username(dto.getUsername())
                .phoneNumber(dto.getPhoneNumber())
                .build());

        return created;
    }

    // ── refresh ───────────────────────────────────────────────────────────────

    @Transactional
    public LoginResponseDto refresh(String refreshToken) {
        if (jwtService.isTokenExpired(refreshToken)) {
            throw new UnauthorizedException("Refresh token has expired");
        }
        String username   = jwtService.extractUsername(refreshToken);
        User   user       = userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        UserPrincipal principal = UserPrincipal.of(user);

        String newAccess  = jwtService.generateAccessToken(principal);
        String newRefresh = jwtService.generateRefreshToken(principal);

        return LoginResponseDto.builder()
                .accessToken(newAccess)
                .refreshToken(newRefresh)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpiration())
                .tenantId(principal.getTenantId())
                .tenantSchema(principal.getTenantSchema())
                .roles(principal.getRoleNames())
                .user(userService.getUserById(principal.getId()))
                .build();
    }
}
