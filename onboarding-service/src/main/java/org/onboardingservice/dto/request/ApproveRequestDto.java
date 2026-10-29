package org.onboardingservice.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApproveRequestDto {

    private boolean approved;

    @Size(max = 500, message = "Rejection reason cannot exceed 500 characters")
    private String rejectionReason;

    @Size(max = 100, message = "Reviewer name cannot exceed 100 characters")
    private String reviewedBy;
}