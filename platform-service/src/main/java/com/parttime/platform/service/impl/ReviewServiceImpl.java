package com.parttime.platform.service.impl;

import com.parttime.platform.service.ReviewService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Override
    public List<Map<String, Object>> list(String isViolation) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", (long) i);
            item.put("workerName", "工人" + i);
            item.put("enterpriseName", "企业" + (i % 5 + 1));
            item.put("jobTitle", "职位" + i);
            item.put("rating", 5 - (i % 3));
            item.put("content", "评价内容" + i + "，这是一条测试评价");
            item.put("isViolation", i % 5 == 0);
            item.put("createdAt", LocalDateTime.now().minusDays(i));
            list.add(item);
        }
        return list;
    }

    @Override
    public Map<String, Object> detail(Long id) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("workerId", 1L);
        result.put("workerName", "测试工人");
        result.put("enterpriseId", 1L);
        result.put("enterpriseName", "测试企业");
        result.put("jobId", 1L);
        result.put("jobTitle", "测试职位");
        result.put("rating", 4);
        result.put("content", "这是一条详细评价内容，包含更多细节信息");
        result.put("reply", "感谢您的评价，我们会继续努力");
        result.put("isViolation", false);
        result.put("createdAt", LocalDateTime.now().minusDays(1));
        return result;
    }

    @Override
    public void markViolation(Long id) {
    }

    @Override
    public void delete(Long id) {
    }
}
