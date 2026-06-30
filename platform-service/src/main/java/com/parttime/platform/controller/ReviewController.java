package com.parttime.platform.controller;

import com.parttime.platform.pojo.cmd.IdCmd;
import com.parttime.platform.pojo.vo.ApiResponse;
import com.parttime.platform.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Operation(summary = "获取评价列表")
    @PostMapping("/list")
    public ApiResponse<List<Map<String, Object>>> list(@RequestBody(required = false) Map<String, String> body) {
        String isViolation = body != null ? body.get("isViolation") : null;
        return ApiResponse.success(reviewService.list(isViolation));
    }

    @Operation(summary = "获取评价详情")
    @PostMapping("/detail")
    public ApiResponse<Map<String, Object>> detail(@RequestBody IdCmd body) {
        return ApiResponse.success(reviewService.detail(body.getId()));
    }

    @Operation(summary = "标记违规评价")
    @PostMapping("/mark-violation")
    public ApiResponse<Void> markViolation(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        reviewService.markViolation(id);
        return ApiResponse.success();
    }

    @Operation(summary = "删除评价")
    @PostMapping("/delete")
    public ApiResponse<Void> delete(@RequestBody IdCmd body) {
        reviewService.delete(body.getId());
        return ApiResponse.success();
    }
}
