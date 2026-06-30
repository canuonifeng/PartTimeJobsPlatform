package com.parttime.platform.service;

import java.util.List;
import java.util.Map;

public interface TransactionService {
    List<Map<String, Object>> list(String type, String status, String keyword);
    Map<String, Object> overview();
}
