package com.parttime.platform.service.impl;

import com.parttime.platform.exception.BusinessException;
import com.parttime.platform.mapper.TrainingCertificationMapper;
import com.parttime.platform.pojo.cmd.TrainingCertificationCmd;
import com.parttime.platform.pojo.entity.TrainingCertification;
import com.parttime.platform.pojo.vo.TrainingCertificationVO;
import com.parttime.platform.service.TrainingCertificationService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

@Service
public class TrainingCertificationServiceImpl implements TrainingCertificationService {

    private static final String ACTIVE = "ACTIVE";
    private static final String DISABLED = "DISABLED";

    @Resource
    private TrainingCertificationMapper trainingCertificationMapper;

    @Override
    public List<TrainingCertificationVO> list() {
        return trainingCertificationMapper.findAll().stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    public TrainingCertificationVO create(TrainingCertificationCmd cmd) {
        if (cmd.getName() == null || cmd.getName().isBlank()) {
            throw new BusinessException("认证名称不能为空");
        }
        if (cmd.getCode() == null || cmd.getCode().isBlank()) {
            throw new BusinessException("认证编码不能为空");
        }
        if (cmd.getTaskType() == null || cmd.getTaskType().isBlank()) {
            throw new BusinessException("适用任务类型不能为空");
        }
        trainingCertificationMapper.findByCode(cmd.getCode().trim())
                .ifPresent(c -> {
                    throw new BusinessException("认证编码已存在: " + cmd.getCode());
                });
        TrainingCertification certification = new TrainingCertification();
        certification.setName(cmd.getName().trim());
        certification.setCode(cmd.getCode().trim());
        certification.setTaskType(cmd.getTaskType().trim());
        certification.setDescription(cmd.getDescription());
        certification.setValidDays(cmd.getValidDays());
        certification.setStatus(ACTIVE);
        trainingCertificationMapper.insert(certification);
        return toVO(certification);
    }

    @Override
    public TrainingCertificationVO update(TrainingCertificationCmd cmd) {
        TrainingCertification certification = trainingCertificationMapper.findById(cmd.getId())
                .orElseThrow(() -> new BusinessException("认证不存在: " + cmd.getId()));
        if (cmd.getName() != null && !cmd.getName().isBlank()) {
            certification.setName(cmd.getName().trim());
        }
        if (cmd.getTaskType() != null && !cmd.getTaskType().isBlank()) {
            certification.setTaskType(cmd.getTaskType().trim());
        }
        certification.setDescription(cmd.getDescription());
        certification.setValidDays(cmd.getValidDays());
        if (cmd.getCode() != null && !cmd.getCode().isBlank()) {
            String code = cmd.getCode().trim();
            trainingCertificationMapper.findByCode(code)
                    .filter(existing -> !existing.getId().equals(certification.getId()))
                    .ifPresent(existing -> {
                        throw new BusinessException("认证编码已存在: " + code);
                    });
            certification.setCode(code);
        }
        trainingCertificationMapper.update(certification);
        return toVO(certification);
    }

    @Override
    public void toggle(Long id, String status) {
        TrainingCertification certification = trainingCertificationMapper.findById(id)
                .orElseThrow(() -> new BusinessException("认证不存在: " + id));
        String targetStatus = ACTIVE.equals(status) ? ACTIVE : DISABLED;
        if (targetStatus.equals(certification.getStatus())) {
            return;
        }
        trainingCertificationMapper.updateStatus(id, targetStatus);
    }

    private TrainingCertificationVO toVO(TrainingCertification certification) {
        TrainingCertificationVO vo = new TrainingCertificationVO();
        vo.setId(certification.getId());
        vo.setName(certification.getName());
        vo.setCode(certification.getCode());
        vo.setTaskType(certification.getTaskType());
        vo.setDescription(certification.getDescription());
        vo.setValidDays(certification.getValidDays());
        vo.setStatus(certification.getStatus());
        vo.setCreatedAt(certification.getCreatedAt());
        vo.setUpdatedAt(certification.getUpdatedAt());
        return vo;
    }
}
