package org.onboardingservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.onboardingservice.messaging.event.TenantApprovedEvent;
import org.onboardingservice.messaging.producer.TenantOnboardingProducer;
import org.onboardingservice.model.OnboardingRequest;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventPublisherService {

    private final TenantOnboardingProducer tenantOnboardingProducer;

    public void publishTenantApproved(OnboardingRequest request) {
        TenantApprovedEvent event = TenantApprovedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .onboardingRequestId(request.getId())
                .companyName(request.getCompanyName())
                .contactEmail(request.getContactEmail())
                .contactName(request.getContactName())
                .phoneNumber(request.getPhoneNumber())
                .businessType(request.getBusinessType())
                .expectedUsers(request.getExpectedUsers())
                .reviewedBy(request.getReviewedBy())
                .build();

        tenantOnboardingProducer.publishTenantApprovedEvent(event);
    }
}