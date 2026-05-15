package com.parttime.platform.core.service;

import com.parttime.platform.api.dto.RegistrationListResponse;
import com.parttime.platform.api.dto.RegistrationResponse;
import com.parttime.platform.api.dto.RegistrationReviewRequest;
import com.parttime.platform.core.domain.EnterpriseRegistration;
import com.parttime.platform.core.exception.BusinessException;
import com.parttime.platform.core.repository.EnterpriseRegistrationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private EnterpriseRegistrationRepository registrationRepository;

    @InjectMocks
    private RegistrationService registrationService;

    @Test
    void getRegistrations_withoutStatus_shouldReturnAll() {
        EnterpriseRegistration reg = createRegistration(1L, "PENDING");
        when(registrationRepository.findAll()).thenReturn(List.of(reg));

        RegistrationListResponse response = registrationService.getRegistrations(null);

        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getTotal()).isEqualTo(1);
    }

    @Test
    void getRegistrations_withStatus_shouldReturnFiltered() {
        EnterpriseRegistration reg = createRegistration(1L, "PENDING");
        when(registrationRepository.findByStatus("PENDING")).thenReturn(List.of(reg));

        RegistrationListResponse response = registrationService.getRegistrations("PENDING");

        assertThat(response.getItems()).hasSize(1);
    }

    @Test
    void getRegistration_shouldReturnRegistration() {
        EnterpriseRegistration reg = createRegistration(1L, "PENDING");
        when(registrationRepository.findById(1L)).thenReturn(Optional.of(reg));

        RegistrationResponse response = registrationService.getRegistration(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getCompanyName()).isEqualTo("TestCompany");
    }

    @Test
    void getRegistration_notFound_shouldThrow() {
        when(registrationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> registrationService.getRegistration(99L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void approveRegistration_shouldApprove() {
        EnterpriseRegistration reg = createRegistration(1L, "PENDING");
        when(registrationRepository.findById(1L)).thenReturn(Optional.of(reg));

        RegistrationResponse response = registrationService.approveRegistration(1L, "admin");

        assertThat(response.getStatus()).isEqualTo("APPROVED");
        assertThat(response.getReviewerId()).isEqualTo("admin");
        verify(registrationRepository).update(any());
    }

    @Test
    void approveRegistration_nonPending_shouldThrow() {
        EnterpriseRegistration reg = createRegistration(1L, "APPROVED");
        when(registrationRepository.findById(1L)).thenReturn(Optional.of(reg));

        assertThatThrownBy(() -> registrationService.approveRegistration(1L, "admin"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not in PENDING");
    }

    @Test
    void rejectRegistration_shouldReject() {
        EnterpriseRegistration reg = createRegistration(1L, "PENDING");
        when(registrationRepository.findById(1L)).thenReturn(Optional.of(reg));

        RegistrationReviewRequest request = new RegistrationReviewRequest();
        request.setRemark("Invalid docs");

        RegistrationResponse response = registrationService.rejectRegistration(1L, "admin", request);

        assertThat(response.getStatus()).isEqualTo("REJECTED");
        assertThat(response.getReviewRemark()).isEqualTo("Invalid docs");
        verify(registrationRepository).update(any());
    }

    private EnterpriseRegistration createRegistration(Long id, String status) {
        EnterpriseRegistration reg = new EnterpriseRegistration();
        reg.setId(id);
        reg.setCompanyName("TestCompany");
        reg.setContactName("Contact");
        reg.setContactPhone("13800138000");
        reg.setStatus(status);
        reg.setCreatedAt(LocalDateTime.now());
        reg.setUpdatedAt(LocalDateTime.now());
        return reg;
    }
}
