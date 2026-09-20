package com.parttime.platform.controller;

import com.parttime.platform.pojo.cmd.CreditScoreAdjustCmd;
import com.parttime.platform.pojo.cmd.IdCmd;
import com.parttime.platform.pojo.cmd.ReviewQueryCmd;
import com.parttime.platform.pojo.cmd.ReviewViolationCmd;
import com.parttime.platform.pojo.vo.ApiResponse;
import com.parttime.platform.pojo.vo.ReviewVO;
import com.parttime.platform.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/reviews")
public class ReviewController {

    @Resource
    private ReviewService reviewService;

    @Operation(summary = "获取评价列表")
    @PostMapping("/list")
    public ApiResponse<List<ReviewVO>> list(@RequestBody(required = false) ReviewQueryCmd body) {
        return ApiResponse.success(reviewService.list(body));
    }

    @Operation(summary = "获取评价详情")
    @PostMapping("/detail")
    public ApiResponse<ReviewVO> detail(@RequestBody IdCmd body) {
        return ApiResponse.success(reviewService.detail(body.getId()));
    }

    @Operation(summary = "标记违规评价")
    @PostMapping("/mark-violation")
    public ApiResponse<Void> markViolation(@RequestBody ReviewViolationCmd body) {
        reviewService.markViolation(body);
        return ApiResponse.success();
    }

    @Operation(summary = "删除评价")
    @PostMapping("/delete")
    public ApiResponse<Void> delete(@RequestBody IdCmd body) {
        reviewService.delete(body.getId());
        return ApiResponse.success();
    }

    @Operation(summary = "调整信用分")
    @PostMapping("/adjust-credit")
    public ApiResponse<Void> adjustCredit(@RequestBody CreditScoreAdjustCmd body) {
        reviewService.adjustCreditScore(body);
        return ApiResponse.success();
    }
}
