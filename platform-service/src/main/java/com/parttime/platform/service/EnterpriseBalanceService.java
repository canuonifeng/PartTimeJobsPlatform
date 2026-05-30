package com.parttime.platform.service;

import com.parttime.platform.pojo.cmd.EnterpriseBalanceAdjustCmd;

public interface EnterpriseBalanceService {
    void adjust(EnterpriseBalanceAdjustCmd cmd);
}
