package com.parttime.enterprise.service.impl;

import com.parttime.enterprise.mapper.EnterpriseMapper;
import com.parttime.enterprise.service.EnterpriseService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

@Service
public class EnterpriseServiceImpl implements EnterpriseService {

    @Resource
    private EnterpriseMapper enterpriseMapper;

    @Override
    public Map<String, Object> getEnterpriseInfo(Long companyId) {
        String name = enterpriseMapper.findCompanyNameById(companyId);
        String logo = enterpriseMapper.findCompanyLogoById(companyId);
        return Map.of("id", companyId, "companyName", name, "companyLogo", logo);
    }

    @Override
    public void updateLogo(Long companyId, String logoUrl) {
        enterpriseMapper.updateLogo(companyId, logoUrl);
    }
}
