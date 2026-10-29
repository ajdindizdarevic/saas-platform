package org.tenantservice.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTenantRequestDto {

    @Size(min = 2, max = 150)
    private String companyName;

    @Size(min = 2, max = 100)
    private String contactName;

    @Pattern(regexp = "^[+]?[0-9]{7,15}$", message = "Invalid phone number")
    private String phoneNumber;

    private String businessType;

    @Min(1) @Max(100_000)
    private Integer expectedUsers;

    @Min(1) @Max(10_000)
    private Integer maxUsers;

    @Min(1) @Max(10_000)
    private Integer maxStorageGb;

    @Size(max = 50)
    private String plan;
}
