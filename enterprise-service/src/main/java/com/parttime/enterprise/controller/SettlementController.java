package com.parttime.enterprise.controller;

import com.parttime.enterprise.config.SecurityUtil;
import com.parttime.enterprise.service.SettlementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/settlement")
public class SettlementController {

    @Resource
    private SettlementService settlementService;

    @Operation(summary = "结算账单列表")
    @GetMapping("/bills")
    public Map<String, Object> listBills(
            @Parameter(description = "工人姓名") @RequestParam(required = false) String workerName,
            @Parameter(description = "开始日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @Parameter(description = "结束日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long companyId = SecurityUtil.getCurrentCompanyId();
        return settlementService.listBills(companyId, workerName, dateFrom, dateTo, page, pageSize);
    }

}
