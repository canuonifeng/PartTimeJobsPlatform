package com.parttime.cservice.service;

import com.parttime.cservice.pojo.vo.EarningsSummaryVO;
import com.parttime.cservice.pojo.vo.WithdrawalVO;

import java.math.BigDecimal;
import java.util.List;

public interface WithdrawalService {
    WithdrawalVO requestWithdrawal(Long workerId, BigDecimal amount);
    List<WithdrawalVO> getWithdrawalHistory(Long workerId);
    EarningsSummaryVO getEarningsSummary(Long workerId);
}
