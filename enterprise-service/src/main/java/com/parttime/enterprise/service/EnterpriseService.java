package com.parttime.enterprise.service;

import java.util.Map;

public interface EnterpriseService {
    Map<String, Object> getEnterpriseInfo(Long companyId);
    void updateLogo(Long companyId, String logoUrl);
}
