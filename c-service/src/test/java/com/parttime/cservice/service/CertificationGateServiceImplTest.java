package com.parttime.cservice.service;

import com.parttime.cservice.mapper.TrainingCertificationMapper;
import com.parttime.cservice.mapper.WorkerCertificationMapper;
import com.parttime.cservice.pojo.entity.TrainingCertification;
import com.parttime.cservice.pojo.entity.WorkerCertification;
import com.parttime.cservice.service.impl.CertificationGateServiceImpl;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CertificationGateServiceImplTest {

    @Mock
    private TrainingCertificationMapper trainingCertificationMapper;

    @Mock
    private WorkerCertificationMapper workerCertificationMapper;

    @InjectMocks
    private CertificationGateServiceImpl gateService;

    private TrainingCertification annotationCert() {
        TrainingCertification cert = new TrainingCertification();
        cert.setId(1L);
        cert.setTaskType("ANNOTATION");
        cert.setStatus("ACTIVE");
        return cert;
    }

    @Test
    void hasCertification_noCertDefined_shouldBeTrue() {
        when(trainingCertificationMapper.findActiveByTaskType("ANNOTATION")).thenReturn(List.of());

        assertThat(gateService.hasCertification(100L, "ANNOTATION")).isTrue();
    }

    @Test
    void hasCertification_noActiveWorkerCert_shouldBeFalse() {
        when(trainingCertificationMapper.findActiveByTaskType("ANNOTATION")).thenReturn(List.of(annotationCert()));
        when(workerCertificationMapper.findByWorkerAndCert(100L, 1L)).thenReturn(Optional.empty());

        assertThat(gateService.hasCertification(100L, "ANNOTATION")).isFalse();
    }

    @Test
    void hasCertification_activeCertWithinValidity_shouldBeTrue() {
        WorkerCertification wc = new WorkerCertification();
        wc.setStatus("ACTIVE");
        wc.setExpiresAt(LocalDateTime.now().plusDays(5));
        when(trainingCertificationMapper.findActiveByTaskType("ANNOTATION")).thenReturn(List.of(annotationCert()));
        when(workerCertificationMapper.findByWorkerAndCert(100L, 1L)).thenReturn(Optional.of(wc));

        assertThat(gateService.hasCertification(100L, "ANNOTATION")).isTrue();
    }

    @Test
    void hasCertification_expiredCert_shouldBeFalse() {
        WorkerCertification wc = new WorkerCertification();
        wc.setStatus("ACTIVE");
        wc.setExpiresAt(LocalDateTime.now().minusDays(1));
        when(trainingCertificationMapper.findActiveByTaskType("ANNOTATION")).thenReturn(List.of(annotationCert()));
        when(workerCertificationMapper.findByWorkerAndCert(100L, 1L)).thenReturn(Optional.of(wc));

        assertThat(gateService.hasCertification(100L, "ANNOTATION")).isFalse();
    }

    @Test
    void checkCertification_withoutCert_shouldThrow() {
        when(trainingCertificationMapper.findActiveByTaskType("ANNOTATION")).thenReturn(List.of(annotationCert()));
        when(workerCertificationMapper.findByWorkerAndCert(100L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gateService.checkCertification(100L, "ANNOTATION"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("需要先完成培训并通过技能认证");
    }

    @Test
    void checkCertification_withCert_shouldPass() {
        WorkerCertification wc = new WorkerCertification();
        wc.setStatus("ACTIVE");
        wc.setExpiresAt(LocalDateTime.now().plusDays(5));
        when(trainingCertificationMapper.findActiveByTaskType("ANNOTATION")).thenReturn(List.of(annotationCert()));
        when(workerCertificationMapper.findByWorkerAndCert(100L, 1L)).thenReturn(Optional.of(wc));

        gateService.checkCertification(100L, "ANNOTATION");
    }
}
