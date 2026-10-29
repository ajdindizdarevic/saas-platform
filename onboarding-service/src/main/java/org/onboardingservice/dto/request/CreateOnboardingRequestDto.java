package org.onboardingservice.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOnboardingRequestDto {

    @NotBlank(message = "Company name is required")
    @Size(min = 2, max = 150, message = "Company name must be between 2 and 150 characters")
    private String companyName;

    @NotBlank(message = "Contact email is required")
    @Email(message = "Invalid email format")
    private String contactEmail;

    @NotBlank(message = "Contact name is required")
    @Size(min = 2, max = 100, message = "Contact name must be between 2 and 100 characters")
    private String contactName;

    @Pattern(regexp = "^[+]?[0-9]{7,15}$", message = "Invalid phone number")
    private String phoneNumber;

    private String businessType;

    @Min(value = 1, message = "Expected users must be at least 1")
    @Max(value = 100000, message = "Expected users cannot exceed 100,000")
    private Integer expectedUsers;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;
}