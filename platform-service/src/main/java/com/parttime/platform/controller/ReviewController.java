package com.parttime.platform.controller;

import com.parttime.platform.pojo.cmd.IdCmd;
import com.parttime.platform.pojo.vo.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/reviews")
public class ReviewController {

    @Operation(summary = "获取评价列表")
    @PostMapping("/list")
    public ApiResponse<List<Map<String, Object>>> list(@RequestBody(required = false) Map<String, String> body) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", i);
            item.put("reviewType", i % 2 == 0 ? "WORKER_TO_ENTERPRISE" : "ENTERPRISE_TO_WORKER");
            item.put("reviewerName", "评价人" + i);
            item.put("revieweeName", "被评价" + i);
            item.put("jobTitle", "职位名称" + i);
            item.put("rating", 5 - (i % 3));
            item.put("content", "这是一条评价内容" + i);
            item.put("isViolation", i % 5 == 0);
            item.put("createdAt", LocalDateTime.now().minusDays(i).toString());
            list.add(item);
        }
        return ApiResponse.success(list);
    }

    @Operation(summary = "获取评价详情")
    @PostMapping("/detail")
    public ApiResponse<Map<String, Object>> detail(@RequestBody IdCmd body) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", body.getId());
        result.put("reviewType", "WORKER_TO_ENTERPRISE");
        result.put("reviewerName", "张三");
        result.put("revieweeName", "XX科技公司");
        result.put("jobTitle", "服务员");
        result.put("rating", 4);
        result.put("content", "公司环境不错，管理规范，薪资发放及时。");
        result.put("isViolation", false);
        result.put("createdAt", LocalDateTime.now().minusDays(5).toString());
        return ApiResponse.success(result);
    }

    @Operation(summary = "标记违规评价")
    @PostMapping("/mark-violation")
    public ApiResponse<Void> markViolation(@RequestBody Map<String, Object> body) {
        return ApiResponse.success();
    }

    @Operation(summary = "删除评价")
    @PostMapping("/delete")
    public ApiResponse<Void> delete(@RequestBody IdCmd body) {
        return ApiResponse.success();
    }
}
