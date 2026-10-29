package org.onboardingservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.onboardingservice.dto.request.ApproveRequestDto;
import org.onboardingservice.dto.response.OnboardingResponseDto;
import org.onboardingservice.exception.BadRequestException;
import org.onboardingservice.mapper.OnboardingMapper;
import org.onboardingservice.model.OnboardingRequest;
import org.onboardingservice.model.RequestStatus;
import org.onboardingservice.repository.OnboardingRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminApprovalService {

    private final OnboardingService onboardingService;
    private final OnboardingRequestRepository repository;
    private final OnboardingMapper mapper;
    private final EventPublisherService eventPublisherService;

    @Transactional
    public OnboardingResponseDto processApproval(UUID requestId, ApproveRequestDto dto) {
        OnboardingRequest request = onboardingService.findById(requestId);

        if (request.getStatus() != RequestStatus.PENDING && request.getStatus() != RequestStatus.UNDER_REVIEW) {
            throw new BadRequestException("Request cannot be processed. Current status: " + request.getStatus());
        }

        if (dto.isApproved()) {
            return approveRequest(request, dto);
        } else {
            return rejectRequest(request, dto);
        }
    }

    @Transactional
    public OnboardingResponseDto markUnderReview(UUID requestId, String reviewedBy) {
        OnboardingRequest request = onboardingService.findById(requestId);

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new BadRequestException("Only PENDING requests can be marked under review");
        }

        request.setStatus(RequestStatus.UNDER_REVIEW);
        request.setReviewedBy(reviewedBy);
        return mapper.toDto(repository.save(request));
    }

    private OnboardingResponseDto approveRequest(OnboardingRequest request, ApproveRequestDto dto) {
        log.info("Approving onboarding request: {}", request.getId());

        request.setStatus(RequestStatus.APPROVED);
        request.setReviewedBy(dto.getReviewedBy());
        OnboardingRequest saved = repository.save(request);

        eventPublisherService.publishTenantApproved(saved);

        log.info("Onboarding request {} approved and event published", request.getId());
        return mapper.toDto(saved);
    }

    /**
     * Re-publishes the TenantApprovedEvent for an already-approved request.
     * Use when the original provisioning pipeline failed (e.g. deserialization error).
     */
    @Transactional
    public OnboardingResponseDto reprovision(UUID requestId) {
        OnboardingRequest request = onboardingService.findById(requestId);
        if (request.getStatus() != RequestStatus.APPROVED) {
            throw new BadRequestException("Only APPROVED requests can be re-provisioned. Current status: " + request.getStatus());
        }
        eventPublisherService.publishTenantApproved(request);
        log.info("Re-provisioning triggered for onboarding request {}", requestId);
        return mapper.toDto(request);
    }

    private OnboardingResponseDto rejectRequest(OnboardingRequest request, ApproveRequestDto dto) {
        log.info("Rejecting onboarding request: {}", request.getId());

        if (dto.getRejectionReason() == null || dto.getRejectionReason().isBlank()) {
            throw new BadRequestException("Rejection reason is required when rejecting a request");
        }

        request.setStatus(RequestStatus.REJECTED);
        request.setRejectionReason(dto.getRejectionReason());
        request.setReviewedBy(dto.getReviewedBy());
        return mapper.toDto(repository.save(request));
    }
}