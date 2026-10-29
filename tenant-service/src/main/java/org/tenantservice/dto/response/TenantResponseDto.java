package org.tenantservice.dto.response;

import lombok.*;
import org.tenantservice.model.TenantStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantResponseDto {

    private UUID id;
    private String companyName;
    private String schemaName;
    private String contactEmail;
    private String contactName;
    private String phoneNumber;
    private String businessType;
    private Integer expectedUsers;
    private Integer maxUsers;
    private Integer maxStorageGb;
    private String plan;
    private TenantStatus status;
    private UUID onboardingRequestId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}