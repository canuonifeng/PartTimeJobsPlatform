package com.parttime.platform.service.impl;

import com.parttime.platform.exception.BusinessException;
import com.parttime.platform.mapper.EnterpriseBalanceTransactionMapper;
import com.parttime.platform.mapper.FinanceReportMapper;
import com.parttime.platform.pojo.cmd.TransactionQueryCmd;
import com.parttime.platform.pojo.vo.TransactionOverviewVO;
import com.parttime.platform.pojo.vo.TransactionVO;
import com.parttime.platform.service.TransactionService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TransactionServiceImpl implements TransactionService {

    private static final Map<String, String> TYPE_NAME = Map.of(
            "TOP_UP", "企业充值",
            "SETTLEMENT", "工资结算",
            "SERVICE_FEE", "平台服务费",
            "REFUND", "退款"
    );

    @Resource
    private EnterpriseBalanceTransactionMapper transactionMapper;

    @Resource
    private FinanceReportMapper financeReportMapper;

    @Override
    public List<TransactionVO> list(TransactionQueryCmd cmd) {
        String dbType = mapType(cmd.getType());
        List<TransactionVO> list = transactionMapper.findByFilters(
                dbType, cmd.getStartDate(), cmd.getEndDate(), cmd.getCompanyId(), cmd.getKeyword());
        return list.stream().peek(this::fillTypeName).collect(Collectors.toList());
    }

    @Override
    public TransactionVO detail(Long id) {
        TransactionVO vo = transactionMapper.findVoById(id)
                .orElseThrow(() -> new BusinessException("交易流水不存在: " + id));
        fillTypeName(vo);
        return vo;
    }

    @Override
    public TransactionOverviewVO overview() {
        TransactionOverviewVO vo = new TransactionOverviewVO();
        vo.setTodayTopUpAmount(nz(transactionMapper.sumByTypeToday("TOP_UP")));
        vo.setTodaySettlementAmount(nz(transactionMapper.sumByTypeToday("SETTLEMENT")));
        vo.setTodayWithdrawalAmount(nz(financeReportMapper.sumCompletedWithdrawalToday()));
        vo.setTodayServiceFeeAmount(BigDecimal.ZERO);
        return vo;
    }

    private String mapType(String frontType) {
        if (frontType == null || frontType.isEmpty()) {
            return null;
        }
        if ("ENTERPRISE_TOPUP".equals(frontType)) {
            return "TOP_UP";
        }
        if ("WORKER_WITHDRAWAL".equals(frontType)) {
            return null;
        }
        return frontType;
    }

    private void fillTypeName(TransactionVO vo) {
        vo.setTypeName(TYPE_NAME.getOrDefault(vo.getType(), vo.getType()));
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
