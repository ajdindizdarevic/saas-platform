package org.onboardingservice.repository;

import org.onboardingservice.model.OnboardingRequest;
import org.onboardingservice.model.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OnboardingRequestRepository extends JpaRepository<OnboardingRequest, UUID> {

    Optional<OnboardingRequest> findByContactEmail(String contactEmail);

    boolean existsByContactEmail(String contactEmail);

    Page<OnboardingRequest> findByStatus(RequestStatus status, Pageable pageable);

    List<OnboardingRequest> findByStatus(RequestStatus status);

    Page<OnboardingRequest> findByCompanyNameContainingIgnoreCase(String companyName, Pageable pageable);
}