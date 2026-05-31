package com.parttime.enterprise.service;

import com.parttime.enterprise.pojo.cmd.EnterpriseRealNameSubmitCmd;
import com.parttime.enterprise.pojo.vo.EnterpriseRealNameAuthVO;

public interface EnterpriseRealNameAuthService {
    EnterpriseRealNameAuthVO submit(Long enterpriseId, EnterpriseRealNameSubmitCmd cmd);
    EnterpriseRealNameAuthVO getStatus(Long enterpriseId);
}
