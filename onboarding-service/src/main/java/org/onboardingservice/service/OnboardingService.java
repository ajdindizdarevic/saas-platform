package org.onboardingservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.onboardingservice.dto.request.CreateOnboardingRequestDto;
import org.onboardingservice.dto.response.OnboardingResponseDto;
import org.onboardingservice.exception.BadRequestException;
import org.onboardingservice.exception.ResourceNotFoundException;
import org.onboardingservice.mapper.OnboardingMapper;
import org.onboardingservice.messaging.event.UserRegisteredEvent;
import org.onboardingservice.model.OnboardingRequest;
import org.onboardingservice.model.RequestStatus;
import org.onboardingservice.repository.OnboardingRequestRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OnboardingService {

    private final OnboardingRequestRepository repository;
    private final OnboardingMapper mapper;

    @Transactional
    public OnboardingResponseDto createRequest(CreateOnboardingRequestDto dto) {
        log.info("Creating onboarding request for company: {}", dto.getCompanyName());

        if (repository.existsByContactEmail(dto.getContactEmail())) {
            throw new BadRequestException("An onboarding request with this email already exists: " + dto.getContactEmail());
        }

        OnboardingRequest request = mapper.toEntity(dto);
        request.setStatus(RequestStatus.PENDING);
        OnboardingRequest saved = repository.save(request);

        log.info("Created onboarding request with id: {}", saved.getId());
        return mapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public OnboardingResponseDto getRequestById(UUID id) {
        OnboardingRequest request = findById(id);
        return mapper.toDto(request);
    }

    @Transactional(readOnly = true)
    public Page<OnboardingResponseDto> getAllRequests(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<OnboardingResponseDto> getRequestsByStatus(RequestStatus status, Pageable pageable) {
        return repository.findByStatus(status, pageable).map(mapper::toDto);
    }

    @Transactional
    public OnboardingResponseDto cancelRequest(UUID id) {
        OnboardingRequest request = findById(id);

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new BadRequestException("Only PENDING requests can be cancelled");
        }

        request.setStatus(RequestStatus.CANCELLED);
        return mapper.toDto(repository.save(request));
    }

    public OnboardingRequest findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Onboarding request not found with id: " + id));
    }

    /**
     * Called by {@link org.onboardingservice.messaging.consumer.UserRegisteredConsumer}
     * when a new user self-registers. Creates a PENDING onboarding entry so admins
     * can review / approve the registration from the dashboard queue.
     */
    @Transactional
    public void createRequestFromRegistration(UserRegisteredEvent event) {
        if (repository.existsByContactEmail(event.getEmail())) {
            log.warn("Onboarding request already exists for email={}, skipping", event.getEmail());
            return;
        }

        String contactName = (event.getFirstName() + " " + event.getLastName()).trim();

        OnboardingRequest request = OnboardingRequest.builder()
                .contactName(contactName)
                .contactEmail(event.getEmail())
                .companyName(contactName)          // placeholder — admin can update on review
                .phoneNumber(event.getPhoneNumber())
                .status(RequestStatus.PENDING)
                .build();

        repository.save(request);
        log.info("Created onboarding request for user={} ({})", event.getUsername(), event.getEmail());
    }
}