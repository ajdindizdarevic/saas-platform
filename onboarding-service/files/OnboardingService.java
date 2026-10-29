package org.saas.onboarding.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.saas.onboarding.dto.request.CreateOnboardingRequestDto;
import org.saas.onboarding.dto.response.OnboardingResponseDto;
import org.saas.onboarding.exception.BadRequestException;
import org.saas.onboarding.exception.ResourceNotFoundException;
import org.saas.onboarding.mapper.OnboardingMapper;
import org.saas.onboarding.model.OnboardingRequest;
import org.saas.onboarding.model.RequestStatus;
import org.saas.onboarding.repository.OnboardingRequestRepository;
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
}
