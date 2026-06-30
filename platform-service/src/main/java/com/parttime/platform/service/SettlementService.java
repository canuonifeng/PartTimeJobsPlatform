package com.parttime.platform.service;

import java.util.List;
import java.util.Map;

public interface SettlementService {
    List<Map<String, Object>> list(String status, String keyword);
    Map<String, Object> detail(Long id);
    void confirm(Long id);
    void cancel(Long id, String reason);
}
