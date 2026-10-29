package org.tenantservice.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTenantRequestDto {

    @NotBlank(message = "Company name is required")
    @Size(min = 2, max = 150)
    private String companyName;

    @NotBlank(message = "Contact email is required")
    @Email(message = "Invalid email format")
    private String contactEmail;

    @NotBlank(message = "Contact name is required")
    @Size(min = 2, max = 100)
    private String contactName;

    @Pattern(regexp = "^[+]?[0-9]{7,15}$", message = "Invalid phone number")
    private String phoneNumber;

    private String businessType;

    @Min(1) @Max(100_000)
    private Integer expectedUsers;

    @Size(max = 50)
    private String plan;
}
