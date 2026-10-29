package org.onboardingservice.mapper;

import org.modelmapper.ModelMapper;
import org.onboardingservice.dto.request.CreateOnboardingRequestDto;
import org.onboardingservice.dto.response.OnboardingResponseDto;
import org.onboardingservice.model.OnboardingRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OnboardingMapper {

    private final ModelMapper modelMapper;

    public OnboardingMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public OnboardingRequest toEntity(CreateOnboardingRequestDto dto) {
        return modelMapper.map(dto, OnboardingRequest.class);
    }

    public OnboardingResponseDto toDto(OnboardingRequest entity) {
        return modelMapper.map(entity, OnboardingResponseDto.class);
    }

    public List<OnboardingResponseDto> toDtoList(List<OnboardingRequest> entities) {
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}