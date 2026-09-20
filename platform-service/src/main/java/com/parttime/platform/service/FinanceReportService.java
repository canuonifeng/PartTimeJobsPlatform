package com.parttime.platform.service;

import com.parttime.platform.pojo.cmd.FinanceQueryCmd;
import com.parttime.platform.pojo.vo.DailyReconVO;
import com.parttime.platform.pojo.vo.ServiceFeeStatVO;

import java.util.List;

public interface FinanceReportService {
    List<DailyReconVO> dailySummary(FinanceQueryCmd cmd);
    ServiceFeeStatVO serviceFeeStats(FinanceQueryCmd cmd);
}
