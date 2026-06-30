package com.parttime.platform.controller;

import com.parttime.platform.pojo.vo.ApiResponse;
import com.parttime.platform.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Operation(summary = "运营概览")
    @PostMapping("/overview")
    public ApiResponse<Map<String, Object>> overview(@RequestBody(required = false) Map<String, String> body) {
        return ApiResponse.success(reportService.overview());
    }

    @Operation(summary = "企业活跃度分析")
    @PostMapping("/enterprise-activity")
    public ApiResponse<List<Map<String, Object>>> enterpriseActivity(@RequestBody(required = false) Map<String, String> body) {
        return ApiResponse.success(reportService.enterpriseActivity());
    }

    @Operation(summary = "工人活跃度分析")
    @PostMapping("/worker-activity")
    public ApiResponse<List<Map<String, Object>>> workerActivity(@RequestBody(required = false) Map<String, String> body) {
        return ApiResponse.success(reportService.workerActivity());
    }

    @Operation(summary = "职位供需分析")
    @PostMapping("/supply-demand")
    public ApiResponse<Map<String, Object>> supplyDemand(@RequestBody(required = false) Map<String, String> body) {
        return ApiResponse.success(reportService.supplyDemandAnalysis());
    }

    @Operation(summary = "转化漏斗分析")
    @PostMapping("/conversion-funnel")
    public ApiResponse<Map<String, Object>> conversionFunnel(@RequestBody(required = false) Map<String, String> body) {
        return ApiResponse.success(reportService.conversionFunnel());
    }
}
