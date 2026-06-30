package com.parttime.platform.service;

import java.util.List;
import java.util.Map;

public interface RiskService {
    List<Map<String, Object>> getBlacklist();
    void addToBlacklist(Map<String, Object> data);
    void removeFromBlacklist(Long id);
    List<Map<String, Object>> getWhitelist();
    List<Map<String, Object>> getRules();
    void toggleRule(Long id, boolean enabled);
}
