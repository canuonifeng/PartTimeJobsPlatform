package com.parttime.platform.service;

import java.util.List;
import java.util.Map;

public interface ReviewService {
    List<Map<String, Object>> list(String isViolation);
    Map<String, Object> detail(Long id);
    void markViolation(Long id);
    void delete(Long id);
}
