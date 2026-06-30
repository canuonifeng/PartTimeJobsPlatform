package com.parttime.platform.service.impl;

import com.parttime.platform.service.RiskService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RiskServiceImpl implements RiskService {

    @Override
    public List<Map<String, Object>> getBlacklist() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", (long) i);
            item.put("type", i % 2 == 0 ? "WORKER" : "ENTERPRISE");
            item.put("targetName", (i % 2 == 0 ? "工人" : "企业") + i);
            item.put("phone", "138" + String.format("%08d", i));
            item.put("reason", "违规原因" + i);
            item.put("operatorName", "管理员" + (i % 3 + 1));
            item.put("createdAt", LocalDateTime.now().minusDays(i));
            list.add(item);
        }
        return list;
    }

    @Override
    public void addToBlacklist(Map<String, Object> data) {
    }

    @Override
    public void removeFromBlacklist(Long id) {
    }

    @Override
    public List<Map<String, Object>> getWhitelist() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", (long) i);
            item.put("type", i % 2 == 0 ? "WORKER" : "ENTERPRISE");
            item.put("targetName", (i % 2 == 0 ? "优质工人" : "优质企业") + i);
            item.put("phone", "139" + String.format("%08d", i));
            item.put("reason", "优质用户白名单");
            item.put("operatorName", "管理员" + (i % 2 + 1));
            item.put("createdAt", LocalDateTime.now().minusDays(i * 10));
            list.add(item);
        }
        return list;
    }

    @Override
    public List<Map<String, Object>> getRules() {
        List<Map<String, Object>> list = new ArrayList<>();
        String[] ruleNames = {"报名频率限制", "打卡距离限制", "提现金额限制", "投诉自动标记", "评价敏感词过滤"};
        for (int i = 0; i < ruleNames.length; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", (long) (i + 1));
            item.put("ruleName", ruleNames[i]);
            item.put("ruleCode", "RULE_" + (i + 1));
            item.put("description", ruleNames[i] + "规则描述");
            item.put("enabled", i != 3);
            list.add(item);
        }
        return list;
    }

    @Override
    public void toggleRule(Long id, boolean enabled) {
    }
}
