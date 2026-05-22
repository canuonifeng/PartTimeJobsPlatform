package com.parttime.cservice.service;

import com.parttime.cservice.pojo.vo.EarningsSummaryVO;
import com.parttime.cservice.pojo.vo.TransactionVO;
import com.parttime.cservice.pojo.vo.WithdrawalVO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface WithdrawalService {
    WithdrawalVO requestWithdrawal(Long workerId, BigDecimal amount);
    List<WithdrawalVO> getWithdrawalHistory(Long workerId);
    EarningsSummaryVO getEarningsSummary(Long workerId);
    Map<String, Object> getTransactions(Long workerId, int page, int pageSize);
}
