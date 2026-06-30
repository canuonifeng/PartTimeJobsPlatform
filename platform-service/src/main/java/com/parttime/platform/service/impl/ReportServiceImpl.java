package com.parttime.platform.service.impl;

import com.parttime.platform.service.ReportService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportServiceImpl implements ReportService {

    @Override
    public Map<String, Object> overview() {
        Map<String, Object> result = new HashMap<>();
        result.put("totalJobs", 156);
        result.put("totalApplications", 1234);
        result.put("totalWorkers", 567);
        result.put("totalEnterprises", 89);
        result.put("totalTransactions", new BigDecimal("123456.78"));
        result.put("totalServiceFee", new BigDecimal("6172.84"));
        return result;
    }

    @Override
    public List<Map<String, Object>> enterpriseActivity() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", LocalDate.now().minusDays(7 - i).toString());
            item.put("newEnterprises", 5 + i * 2);
            item.put("activeEnterprises", 50 + i * 5);
            item.put("publishedJobs", 20 + i * 3);
            list.add(item);
        }
        return list;
    }

    @Override
    public List<Map<String, Object>> workerActivity() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", LocalDate.now().minusDays(7 - i).toString());
            item.put("newWorkers", 10 + i * 3);
            item.put("activeWorkers", 100 + i * 10);
            item.put("applications", 50 + i * 15);
            list.add(item);
        }
        return list;
    }

    @Override
    public Map<String, Object> supplyDemandAnalysis() {
        Map<String, Object> result = new HashMap<>();
        result.put("totalDemand", 500);
        result.put("totalSupply", 450);
        result.put("matchingRate", new BigDecimal("90.0"));
        List<Map<String, Object>> categories = new ArrayList<>();
        String[] categoryNames = {"餐饮", "零售", "物流", "家政", "客服"};
        for (int i = 0; i < categoryNames.length; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("category", categoryNames[i]);
            item.put("demand", 100 - i * 10);
            item.put("supply", 90 - i * 8);
            categories.add(item);
        }
        result.put("categories", categories);
        return result;
    }

    @Override
    public Map<String, Object> conversionFunnel() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> steps = new ArrayList<>();
        steps.add(createFunnelStep("浏览职位", 10000));
        steps.add(createFunnelStep("点击详情", 5000));
        steps.add(createFunnelStep("一键报名", 2000));
        steps.add(createFunnelStep("企业录用", 1500));
        steps.add(createFunnelStep("到岗工作", 1200));
        steps.add(createFunnelStep("完成结算", 1000));
        result.put("steps", steps);
        return result;
    }

    private Map<String, Object> createFunnelStep(String name, int count) {
        Map<String, Object> step = new HashMap<>();
        step.put("name", name);
        step.put("count", count);
        return step;
    }
}
