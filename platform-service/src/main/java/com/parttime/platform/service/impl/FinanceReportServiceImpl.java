package com.parttime.platform.service.impl;

import com.parttime.platform.mapper.EnterpriseBalanceTransactionMapper;
import com.parttime.platform.mapper.FinanceReportMapper;
import com.parttime.platform.pojo.cmd.FinanceQueryCmd;
import com.parttime.platform.pojo.po.DailyAggRow;
import com.parttime.platform.pojo.vo.CategoryFeeVO;
import com.parttime.platform.pojo.vo.DailyReconVO;
import com.parttime.platform.pojo.vo.FeePointVO;
import com.parttime.platform.pojo.vo.ServiceFeeStatVO;
import com.parttime.platform.service.FinanceReportService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FinanceReportServiceImpl implements FinanceReportService {

    @Resource
    private FinanceReportMapper financeReportMapper;

    @Resource
    private EnterpriseBalanceTransactionMapper transactionMapper;

    @Override
    public List<DailyReconVO> dailySummary(FinanceQueryCmd cmd) {
        String[] range = resolveRange(cmd);
        String start = range[0];
        String end = range[1];

        Map<String, BigDecimal> topUpMap = toAmountMap(financeReportMapper.sumApprovedTopUpGroupByDate(start, end));
        Map<String, BigDecimal> withdrawalMap = toAmountMap(financeReportMapper.sumCompletedWithdrawalGroupByDate(start, end));
        Map<String, Long> settleMap = toCountMap(transactionMapper.countByTypeGroupByDate("SETTLEMENT", start, end));

        List<DailyReconVO> result = new ArrayList<>();
        LocalDate cursor = LocalDate.parse(end);
        LocalDate from = LocalDate.parse(start);
        List<DailyReconVO> desc = new ArrayList<>();
        while (!cursor.isBefore(from)) {
            String day = cursor.toString();
            DailyReconVO vo = new DailyReconVO();
            vo.setDate(day);
            vo.setTotalTopUp(topUpMap.getOrDefault(day, BigDecimal.ZERO));
            vo.setTotalWithdrawal(withdrawalMap.getOrDefault(day, BigDecimal.ZERO));
            vo.setTotalServiceFee(BigDecimal.ZERO);
            vo.setSettlementCount(settleMap.getOrDefault(day, 0L).intValue());
            vo.setStatus("RECONCILED");
            desc.add(vo);
            cursor = cursor.minusDays(1);
        }
        java.util.Collections.reverse(desc);
        return desc;
    }

    @Override
    public ServiceFeeStatVO serviceFeeStats(FinanceQueryCmd cmd) {
        String[] range = resolveRange(cmd);
        String start = range[0];
        String end = range[1];

        ServiceFeeStatVO vo = new ServiceFeeStatVO();
        vo.setTotalServiceFee(BigDecimal.ZERO);
        vo.setTodayServiceFee(BigDecimal.ZERO);
        vo.setWeekServiceFee(BigDecimal.ZERO);
        vo.setMonthServiceFee(BigDecimal.ZERO);

        List<FeePointVO> trend = new ArrayList<>();
        LocalDate cursor = LocalDate.parse(start);
        LocalDate to = LocalDate.parse(end);
        while (!cursor.isAfter(to)) {
            FeePointVO point = new FeePointVO();
            point.setDate(cursor.toString());
            point.setAmount(BigDecimal.ZERO);
            trend.add(point);
            cursor = cursor.plusDays(1);
        }
        vo.setTrend(trend);
        vo.setByCategory(new ArrayList<CategoryFeeVO>());
        return vo;
    }

    private String[] resolveRange(FinanceQueryCmd cmd) {
        LocalDate end = (cmd != null && cmd.getEndDate() != null && !cmd.getEndDate().isEmpty())
                ? LocalDate.parse(cmd.getEndDate()) : LocalDate.now();
        LocalDate start = (cmd != null && cmd.getStartDate() != null && !cmd.getStartDate().isEmpty())
                ? LocalDate.parse(cmd.getStartDate()) : end.minusDays(6);
        return new String[]{start.toString(), end.toString()};
    }

    private Map<String, BigDecimal> toAmountMap(List<DailyAggRow> rows) {
        Map<String, BigDecimal> map = new HashMap<>();
        for (DailyAggRow row : rows) {
            map.put(row.getDate(), row.getAmount() == null ? BigDecimal.ZERO : row.getAmount());
        }
        return map;
    }

    private Map<String, Long> toCountMap(List<DailyAggRow> rows) {
        Map<String, Long> map = new HashMap<>();
        for (DailyAggRow row : rows) {
            map.put(row.getDate(), row.getCount() == null ? 0L : row.getCount());
        }
        return map;
    }
}
