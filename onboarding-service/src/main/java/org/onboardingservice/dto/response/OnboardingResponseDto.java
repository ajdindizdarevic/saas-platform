package org.onboardingservice.dto.response;

import lombok.*;
import org.onboardingservice.model.RequestStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OnboardingResponseDto {

    private UUID id;
    private String companyName;
    private String contactEmail;
    private String contactName;
    private String phoneNumber;
    private String businessType;
    private Integer expectedUsers;
    private String notes;
    private RequestStatus status;
    private String rejectionReason;
    private String reviewedBy;
    private String tenantId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}