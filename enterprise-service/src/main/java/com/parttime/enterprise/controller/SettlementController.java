package com.parttime.enterprise.controller;

import com.parttime.enterprise.config.SecurityUtil;
import com.parttime.enterprise.pojo.vo.ApiResponse;
import com.parttime.enterprise.service.SettlementService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/settlement")
public class SettlementController {

    @Resource
    private SettlementService settlementService;

    @Operation(summary = "撤回结算", description = "将已结算的考勤记录撤回，扣减工人余额")
    @PutMapping("/unsettle")
    public void unsettle(@RequestParam Long attendanceRecordId) {
        Long companyId = SecurityUtil.getCurrentCompanyId();
        settlementService.unsettle(attendanceRecordId, companyId);
    }

}
