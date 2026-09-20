package com.parttime.platform.service.impl;

import com.parttime.platform.mapper.DashboardTrendMapper;
import com.parttime.platform.pojo.vo.DashboardTrendVO;
import com.parttime.platform.pojo.vo.DashboardVO;
import com.parttime.platform.service.DashboardService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardServiceImpl implements DashboardService {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Resource
    private DashboardTrendMapper dashboardTrendMapper;

    @Override
    public DashboardVO getDashboardStats() {
        DashboardVO vo = new DashboardVO();
        vo.setTotalCompanies(dashboardTrendMapper.countTotalCompanies());
        vo.setActiveJobs(dashboardTrendMapper.countActiveJobs());
        vo.setTotalWorkers(dashboardTrendMapper.countTotalWorkers());
        vo.setCompletedShifts(dashboardTrendMapper.countCompletedShiftsTotal());
        BigDecimal amount = dashboardTrendMapper.sumTransactionAmount();
        vo.setTotalTransactionAmount(amount == null ? BigDecimal.ZERO : amount);
        vo.setRecentTrend(dashboardTrendMapper.countCompletedShifts(
                LocalDate.now().minusDays(6L).format(DAY) + " 00:00:00"));
        return vo;
    }

    @Override
    public DashboardTrendVO getTrend(Integer days) {
        int n = (days == null || days <= 0) ? 7 : Math.min(days, 90);
        LocalDate start = LocalDate.now().minusDays(n - 1L);
        String startStr = start.format(DAY) + " 00:00:00";

        Map<String, Integer> jobMap = toMap(dashboardTrendMapper.countNewJobs(startStr));
        Map<String, Integer> workerMap = toMap(dashboardTrendMapper.countNewWorkers(startStr));
        Map<String, Integer> shiftMap = toMap(dashboardTrendMapper.countCompletedShifts(startStr));

        List<String> dates = new ArrayList<>();
        List<Integer> newJobs = new ArrayList<>();
        List<Integer> newWorkers = new ArrayList<>();
        List<Integer> completedShifts = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            String d = start.plusDays(i).format(DAY);
            dates.add(d);
            newJobs.add(jobMap.getOrDefault(d, 0));
            newWorkers.add(workerMap.getOrDefault(d, 0));
            completedShifts.add(shiftMap.getOrDefault(d, 0));
        }

        DashboardTrendVO vo = new DashboardTrendVO();
        vo.setDates(dates);
        vo.setNewJobs(newJobs);
        vo.setNewWorkers(newWorkers);
        vo.setCompletedShifts(completedShifts);
        return vo;
    }

    private Map<String, Integer> toMap(List<DashboardVO.TrendDataPoint> rows) {
        Map<String, Integer> map = new HashMap<>();
        if (rows == null) {
            return map;
        }
        for (DashboardVO.TrendDataPoint row : rows) {
            map.put(row.getDate(), row.getCount());
        }
        return map;
    }
}
