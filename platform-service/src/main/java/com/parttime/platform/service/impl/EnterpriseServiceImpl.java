package com.parttime.platform.service.impl;
import com.parttime.platform.exception.BusinessException;
import com.parttime.platform.mapper.EnterpriseMapper;
import com.parttime.platform.pojo.cmd.EnterpriseCreateCmd;
import com.parttime.platform.pojo.cmd.EnterpriseUpdateCmd;
import com.parttime.platform.pojo.entity.Enterprise;
import com.parttime.platform.pojo.vo.EnterpriseVO;
import com.parttime.platform.service.EnterpriseService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EnterpriseServiceImpl implements EnterpriseService {

    @Resource
    private EnterpriseMapper enterpriseMapper;

    @Override
    public List<EnterpriseVO> list(String status) {
        List<Enterprise> list;
        if (status != null && !status.isBlank()) {
            list = enterpriseMapper.findByStatus(status);
        } else {
            list = enterpriseMapper.findAll();
        }
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public EnterpriseVO detail(Long id) {
        Enterprise e = enterpriseMapper.findById(id)
                .orElseThrow(() -> new BusinessException("Enterprise not found: " + id));
        return toVO(e);
    }

    @Override
    public EnterpriseVO create(EnterpriseCreateCmd cmd) {
        Enterprise e = new Enterprise();
        e.setCompanyName(cmd.getCompanyName());
        e.setContactName(cmd.getContactName());
        e.setContactPhone(cmd.getContactPhone());
        e.setCompanyAddress(cmd.getCompanyAddress());
        e.setBusinessLicense(cmd.getBusinessLicense());
        e.setStatus("ACTIVE");
        enterpriseMapper.insert(e);
        return toVO(e);
    }

    @Override
    public EnterpriseVO update(EnterpriseUpdateCmd cmd) {
        Enterprise e = enterpriseMapper.findById(cmd.getId())
                .orElseThrow(() -> new BusinessException("Enterprise not found: " + cmd.getId()));
        if (cmd.getCompanyName() != null) e.setCompanyName(cmd.getCompanyName());
        if (cmd.getContactName() != null) e.setContactName(cmd.getContactName());
        if (cmd.getContactPhone() != null) e.setContactPhone(cmd.getContactPhone());
        if (cmd.getCompanyAddress() != null) e.setCompanyAddress(cmd.getCompanyAddress());
        if (cmd.getBusinessLicense() != null) e.setBusinessLicense(cmd.getBusinessLicense());
        enterpriseMapper.update(e);
        return toVO(e);
    }

    @Override
    public void suspend(Long id) {
        Enterprise e = enterpriseMapper.findById(id)
                .orElseThrow(() -> new BusinessException("Enterprise not found: " + id));
        if (!"ACTIVE".equals(e.getStatus())) {
            throw new BusinessException("Enterprise is not ACTIVE");
        }
        enterpriseMapper.updateStatus(id, "SUSPENDED");
    }

    @Override
    public void activate(Long id) {
        Enterprise e = enterpriseMapper.findById(id)
                .orElseThrow(() -> new BusinessException("Enterprise not found: " + id));
        if (!"SUSPENDED".equals(e.getStatus())) {
            throw new BusinessException("Enterprise is not SUSPENDED");
        }
        enterpriseMapper.updateStatus(id, "ACTIVE");
    }

    private EnterpriseVO toVO(Enterprise e) {
        EnterpriseVO vo = new EnterpriseVO();
        vo.setId(e.getId());
        vo.setCompanyName(e.getCompanyName());
        vo.setContactName(e.getContactName());
        vo.setContactPhone(e.getContactPhone());
        vo.setCompanyAddress(e.getCompanyAddress());
        vo.setBusinessLicense(e.getBusinessLicense());
        vo.setStatus(e.getStatus());
        vo.setRegistrationId(e.getRegistrationId());
        vo.setCreatedAt(e.getCreatedAt());
        vo.setUpdatedAt(e.getUpdatedAt());
        return vo;
    }
}
