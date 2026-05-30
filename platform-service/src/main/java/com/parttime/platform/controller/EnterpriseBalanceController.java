package com.parttime.platform.controller;

import com.parttime.platform.pojo.cmd.EnterpriseBalanceAdjustCmd;
import com.parttime.platform.service.EnterpriseBalanceService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/enterprise/balance")
public class EnterpriseBalanceController {

    @Resource
    private EnterpriseBalanceService enterpriseBalanceService;

    @Operation(summary = "运营后台直接调整企业余额（充值/扣款）")
    @PostMapping("/adjust")
    public void adjust(@RequestBody EnterpriseBalanceAdjustCmd cmd) {
        enterpriseBalanceService.adjust(cmd);
    }
}
