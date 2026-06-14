package com.parttime.enterprise.controller;

import com.parttime.enterprise.config.SecurityUtil;
import com.parttime.enterprise.pojo.cmd.SettlementUnsettleCmd;
import com.parttime.enterprise.service.SettlementService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/enterprise/settlement")
public class SettlementController {

    @Resource
    private SettlementService settlementService;

    @Operation(summary = "撤回结算", description = "将已结算的考勤记录撤回，扣减工人余额")
    @PostMapping("/unsettle")
    public void unsettle(@RequestBody SettlementUnsettleCmd cmd) {
        Long companyId = SecurityUtil.getCurrentCompanyId();
        settlementService.unsettle(cmd.getAttendanceRecordId(), companyId);
    }

}
