package com.parttime.enterprise.service.impl;

import com.parttime.enterprise.mapper.EnterpriseMapper;
import com.parttime.enterprise.pojo.vo.EnterpriseInfoVO;
import com.parttime.enterprise.service.EnterpriseService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class EnterpriseServiceImpl implements EnterpriseService {

    @Resource
    private EnterpriseMapper enterpriseMapper;

    @Override
    public EnterpriseInfoVO getEnterpriseInfo(Long companyId) {
        String name = enterpriseMapper.findCompanyNameById(companyId);
        String logo = enterpriseMapper.findCompanyLogoById(companyId);
        EnterpriseInfoVO vo = new EnterpriseInfoVO();
        vo.setId(companyId);
        vo.setCompanyName(name);
        vo.setCompanyLogo(logo);
        return vo;
    }

    @Override
    public void updateLogo(Long companyId, String logoUrl) {
        enterpriseMapper.updateLogo(companyId, logoUrl);
    }
}
