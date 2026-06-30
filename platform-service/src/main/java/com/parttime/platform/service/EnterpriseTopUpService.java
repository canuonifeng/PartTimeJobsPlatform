package com.parttime.platform.service;

import com.parttime.platform.pojo.entity.EnterpriseTopUp;
import java.util.List;

public interface EnterpriseTopUpService {
    List<EnterpriseTopUp> list(String status, String keyword);
    EnterpriseTopUp detail(Long id);
    void approve(Long id, String auditor, String remark);
    void reject(Long id, String auditor, String remark);
}
