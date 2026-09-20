package com.parttime.cservice.service.impl;

import com.parttime.cservice.mapper.TrainingCertificationMapper;
import com.parttime.cservice.mapper.WorkerCertificationMapper;
import com.parttime.cservice.pojo.entity.TrainingCertification;
import com.parttime.cservice.pojo.entity.WorkerCertification;
import com.parttime.cservice.service.CertificationGateService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CertificationGateServiceImpl implements CertificationGateService {

    private static final String ACTIVE = "ACTIVE";

    @Resource
    private TrainingCertificationMapper trainingCertificationMapper;

    @Resource
    private WorkerCertificationMapper workerCertificationMapper;

    @Override
    public boolean hasCertification(Long workerId, String taskType) {
        if (workerId == null || taskType == null || taskType.isBlank()) {
            return false;
        }
        List<TrainingCertification> certs = trainingCertificationMapper.findActiveByTaskType(taskType);
        if (certs.isEmpty()) {
            // 该任务类型未配置认证要求，视为无需认证
            return true;
        }
        LocalDateTime now = LocalDateTime.now();
        for (TrainingCertification cert : certs) {
            WorkerCertification wc = workerCertificationMapper.findByWorkerAndCert(workerId, cert.getId()).orElse(null);
            if (wc == null || !ACTIVE.equals(wc.getStatus())) {
                continue;
            }
            if (wc.getExpiresAt() == null || wc.getExpiresAt().isAfter(now)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void checkCertification(Long workerId, String taskType) {
        if (!hasCertification(workerId, taskType)) {
            throw new RuntimeException("需要先完成培训并通过技能认证，才能抢该类任务");
        }
    }
}
