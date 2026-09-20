package com.parttime.platform.controller;

import com.parttime.platform.pojo.cmd.FinanceQueryCmd;
import com.parttime.platform.pojo.vo.ApiResponse;
import com.parttime.platform.pojo.vo.DailyReconVO;
import com.parttime.platform.pojo.vo.ServiceFeeStatVO;
import com.parttime.platform.service.FinanceReportService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/finance")
public class FinanceReportController {

    @Resource
    private FinanceReportService financeReportService;

    @Operation(summary = "获取每日对账概览")
    @PostMapping("/daily-summary")
    public ApiResponse<List<DailyReconVO>> dailySummary(@RequestBody(required = false) FinanceQueryCmd body) {
        return ApiResponse.success(financeReportService.dailySummary(body));
    }

    @Operation(summary = "服务费统计")
    @PostMapping("/service-fee-stats")
    public ApiResponse<ServiceFeeStatVO> serviceFeeStats(@RequestBody(required = false) FinanceQueryCmd body) {
        return ApiResponse.success(financeReportService.serviceFeeStats(body));
    }
}
