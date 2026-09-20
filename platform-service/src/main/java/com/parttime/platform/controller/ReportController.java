package com.parttime.platform.controller;

import com.parttime.platform.pojo.cmd.IdCmd;
import com.parttime.platform.pojo.vo.ApiResponse;
import com.parttime.platform.pojo.vo.ConversionFunnelVO;
import com.parttime.platform.pojo.vo.EnterpriseActivityVO;
import com.parttime.platform.pojo.vo.EnterpriseTrendVO;
import com.parttime.platform.pojo.vo.OverviewVO;
import com.parttime.platform.pojo.vo.SupplyDemandVO;
import com.parttime.platform.pojo.vo.WorkerActivityVO;
import com.parttime.platform.pojo.vo.WorkerTrendVO;
import com.parttime.platform.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/reports")
public class ReportController {

    @Resource
    private ReportService reportService;

    @Operation(summary = "运营概览")
    @PostMapping("/overview")
    public ApiResponse<OverviewVO> overview(@RequestBody(required = false) IdCmd body) {
        return ApiResponse.success(reportService.overview());
    }

    @Operation(summary = "企业活跃度趋势")
    @PostMapping("/enterprise-activity")
    public ApiResponse<List<EnterpriseTrendVO>> enterpriseActivity(@RequestBody(required = false) IdCmd body) {
        return ApiResponse.success(reportService.enterpriseActivity());
    }

    @Operation(summary = "企业活跃度排名")
    @PostMapping("/enterprise-ranking")
    public ApiResponse<List<EnterpriseActivityVO>> enterpriseRanking(@RequestBody(required = false) IdCmd body) {
        return ApiResponse.success(reportService.enterpriseRanking());
    }

    @Operation(summary = "工人活跃度趋势")
    @PostMapping("/worker-activity")
    public ApiResponse<List<WorkerTrendVO>> workerActivity(@RequestBody(required = false) IdCmd body) {
        return ApiResponse.success(reportService.workerActivity());
    }

    @Operation(summary = "工人活跃度排名")
    @PostMapping("/worker-ranking")
    public ApiResponse<List<WorkerActivityVO>> workerRanking(@RequestBody(required = false) IdCmd body) {
        return ApiResponse.success(reportService.workerRanking());
    }

    @Operation(summary = "职位供需分析")
    @PostMapping("/supply-demand")
    public ApiResponse<SupplyDemandVO> supplyDemand(@RequestBody(required = false) IdCmd body) {
        return ApiResponse.success(reportService.supplyDemandAnalysis());
    }

    @Operation(summary = "转化漏斗分析")
    @PostMapping("/conversion-funnel")
    public ApiResponse<ConversionFunnelVO> conversionFunnel(@RequestBody(required = false) IdCmd body) {
        return ApiResponse.success(reportService.conversionFunnel());
    }
}
