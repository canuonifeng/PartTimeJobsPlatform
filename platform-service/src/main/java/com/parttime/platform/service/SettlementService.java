package com.parttime.platform.service;

import com.parttime.platform.pojo.cmd.SettlementQueryCmd;
import com.parttime.platform.pojo.vo.SettlementVO;

import java.util.List;

public interface SettlementService {
    List<SettlementVO> list(SettlementQueryCmd cmd);
    SettlementVO detail(Long id);
    void confirm(Long id);
    void cancel(Long id, String reason);
}
