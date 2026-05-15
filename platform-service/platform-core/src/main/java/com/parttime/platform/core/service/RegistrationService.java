package com.parttime.platform.core.service;

import com.parttime.platform.api.dto.RegistrationListResponse;
import com.parttime.platform.api.dto.RegistrationResponse;
import com.parttime.platform.api.dto.RegistrationReviewRequest;
import com.parttime.platform.core.domain.EnterpriseRegistration;
import com.parttime.platform.core.exception.BusinessException;
import com.parttime.platform.core.repository.EnterpriseRegistrationRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class RegistrationService {

    private final EnterpriseRegistrationRepository registrationRepository;

    public RegistrationService(EnterpriseRegistrationRepository registrationRepository) {
        this.registrationRepository = registrationRepository;
    }

    public RegistrationListResponse getRegistrations(String status) {
        List<EnterpriseRegistration> list;
        if (status != null && !status.isBlank()) {
            list = registrationRepository.findByStatus(status);
        } else {
            list = registrationRepository.findAll();
        }
        List<RegistrationResponse> items = list.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        RegistrationListResponse response = new RegistrationListResponse();
        response.setItems(items);
        response.setTotal(items.size());
        return response;
    }

    public RegistrationResponse getRegistration(Long id) {
        EnterpriseRegistration registration = registrationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Registration not found: " + id));
        return toResponse(registration);
    }

    public RegistrationResponse approveRegistration(Long id, String reviewerId) {
        EnterpriseRegistration registration = registrationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Registration not found: " + id));
        if (!"PENDING".equals(registration.getStatus())) {
            throw new BusinessException("Registration is not in PENDING status");
        }
        registration.setStatus("APPROVED");
        registration.setReviewerId(reviewerId);
        registration.setReviewedAt(LocalDateTime.now());
        registrationRepository.update(registration);
        return toResponse(registration);
    }

    public RegistrationResponse rejectRegistration(Long id, String reviewerId, RegistrationReviewRequest request) {
        EnterpriseRegistration registration = registrationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Registration not found: " + id));
        if (!"PENDING".equals(registration.getStatus())) {
            throw new BusinessException("Registration is not in PENDING status");
        }
        registration.setStatus("REJECTED");
        registration.setReviewerId(reviewerId);
        registration.setReviewRemark(request.getRemark());
        registration.setReviewedAt(LocalDateTime.now());
        registrationRepository.update(registration);
        return toResponse(registration);
    }

    private RegistrationResponse toResponse(EnterpriseRegistration reg) {
        RegistrationResponse response = new RegistrationResponse();
        response.setId(reg.getId());
        response.setCompanyName(reg.getCompanyName());
        response.setContactName(reg.getContactName());
        response.setContactPhone(reg.getContactPhone());
        response.setCompanyAddress(reg.getCompanyAddress());
        response.setBusinessLicense(reg.getBusinessLicense());
        response.setStatus(reg.getStatus());
        response.setReviewerId(reg.getReviewerId());
        response.setReviewRemark(reg.getReviewRemark());
        response.setReviewedAt(reg.getReviewedAt());
        response.setCreatedAt(reg.getCreatedAt());
        response.setUpdatedAt(reg.getUpdatedAt());
        return response;
    }
}
