package org.userservice.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import org.userservice.model.RoleName;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserDto {

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 100)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 100)
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email
    private String email;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50)
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8)
    private String password;

    @Pattern(regexp = "^[+]?[0-9]{7,15}$")
    private String phoneNumber;

    @NotBlank(message = "Tenant ID is required")
    private String tenantId;

    private String tenantSchema;

    private UUID onboardingRequestId;

    /** Roles to assign; defaults to USER if omitted */
    private Set<RoleName> roles;
}
