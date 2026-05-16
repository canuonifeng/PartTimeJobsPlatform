package com.parttime.platform.service.impl;

import com.parttime.platform.exception.BusinessException;
import com.parttime.platform.mapper.EnterpriseMapper;
import com.parttime.platform.mapper.EnterpriseRegistrationMapper;
import com.parttime.platform.pojo.cmd.ReviewRegistrationCmd;
import com.parttime.platform.pojo.entity.Enterprise;
import com.parttime.platform.pojo.entity.EnterpriseRegistration;
import com.parttime.platform.pojo.vo.RegistrationListVO;
import com.parttime.platform.pojo.vo.RegistrationVO;
import com.parttime.platform.service.RegistrationService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RegistrationServiceImpl implements RegistrationService {

    @Resource
    private EnterpriseRegistrationMapper registrationMapper;

    @Resource
    private EnterpriseMapper enterpriseMapper;

    @Override
    public RegistrationListVO getRegistrations(String status) {
        List<EnterpriseRegistration> list;
        if (status != null && !status.isBlank()) {
            list = registrationMapper.findByStatus(status);
        } else {
            list = registrationMapper.findAll();
        }
        List<RegistrationVO> items = list.stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        RegistrationListVO response = new RegistrationListVO();
        response.setItems(items);
        response.setTotal(items.size());
        return response;
    }

    @Override
    public RegistrationVO getRegistration(Long id) {
        EnterpriseRegistration registration = registrationMapper.findById(id)
                .orElseThrow(() -> new BusinessException("Registration not found: " + id));
        return toVO(registration);
    }

    @Override
    public RegistrationVO approveRegistration(Long id, String reviewerId) {
        EnterpriseRegistration registration = registrationMapper.findById(id)
                .orElseThrow(() -> new BusinessException("Registration not found: " + id));
        if (!"PENDING".equals(registration.getStatus())) {
            throw new BusinessException("Registration is not in PENDING status");
        }
        registration.setStatus("APPROVED");
        registration.setReviewerId(reviewerId);
        registration.setReviewedAt(LocalDateTime.now());
        registrationMapper.update(registration);

        Enterprise enterprise = new Enterprise();
        enterprise.setCompanyName(registration.getCompanyName());
        enterprise.setContactName(registration.getContactName());
        enterprise.setContactPhone(registration.getContactPhone());
        enterprise.setCompanyAddress(registration.getCompanyAddress());
        enterprise.setBusinessLicense(registration.getBusinessLicense());
        enterprise.setStatus("ACTIVE");
        enterprise.setRegistrationId(registration.getId());
        enterpriseMapper.insert(enterprise);

        return toVO(registration);
    }

    @Override
    public RegistrationVO rejectRegistration(Long id, String reviewerId, ReviewRegistrationCmd cmd) {
        EnterpriseRegistration registration = registrationMapper.findById(id)
                .orElseThrow(() -> new BusinessException("Registration not found: " + id));
        if (!"PENDING".equals(registration.getStatus())) {
            throw new BusinessException("Registration is not in PENDING status");
        }
        registration.setStatus("REJECTED");
        registration.setReviewerId(reviewerId);
        registration.setReviewRemark(cmd.getRemark());
        registration.setReviewedAt(LocalDateTime.now());
        registrationMapper.update(registration);
        return toVO(registration);
    }

    private RegistrationVO toVO(EnterpriseRegistration reg) {
        RegistrationVO vo = new RegistrationVO();
        vo.setId(reg.getId());
        vo.setCompanyName(reg.getCompanyName());
        vo.setContactName(reg.getContactName());
        vo.setContactPhone(reg.getContactPhone());
        vo.setCompanyAddress(reg.getCompanyAddress());
        vo.setBusinessLicense(reg.getBusinessLicense());
        vo.setStatus(reg.getStatus());
        vo.setReviewerId(reg.getReviewerId());
        vo.setReviewRemark(reg.getReviewRemark());
        vo.setReviewedAt(reg.getReviewedAt());
        vo.setCreatedAt(reg.getCreatedAt());
        vo.setUpdatedAt(reg.getUpdatedAt());
        return vo;
    }
}
