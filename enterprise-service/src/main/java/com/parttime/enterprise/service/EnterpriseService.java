package com.parttime.enterprise.service;

import com.parttime.enterprise.pojo.vo.EnterpriseInfoVO;

public interface EnterpriseService {
    EnterpriseInfoVO getEnterpriseInfo(Long companyId);
    void updateLogo(Long companyId, String logoUrl);
}
