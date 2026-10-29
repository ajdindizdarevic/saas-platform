package org.userservice.dto.response;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDto {

    private String accessToken;

    @Builder.Default
    private String tokenType = "Bearer";

    private long expiresIn;   // seconds

    private String refreshToken;

    private UserResponseDto user;

    private String tenantId;
    private String tenantSchema;

    private Set<String> roles;
}