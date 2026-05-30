package com.parttime.enterprise.controller;

import com.parttime.enterprise.config.SecurityUtil;
import com.parttime.enterprise.pojo.vo.EnterpriseBalanceVO;
import com.parttime.enterprise.pojo.vo.EnterpriseTransactionVO;
import com.parttime.enterprise.pojo.vo.PageVO;
import com.parttime.enterprise.service.EnterpriseBalanceService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/enterprise/balance")
public class EnterpriseBalanceController {

    @Resource
    private EnterpriseBalanceService enterpriseBalanceService;

    @Operation(summary = "查询企业余额")
    @GetMapping("")
    public EnterpriseBalanceVO getBalance() {
        Long companyId = SecurityUtil.getCurrentCompanyId();
        return enterpriseBalanceService.getBalance(companyId);
    }

    @Operation(summary = "企业充值（模拟支付）")
    @PostMapping("/top-up")
    public void topUp(@RequestBody Map<String, BigDecimal> body) {
        Long companyId = SecurityUtil.getCurrentCompanyId();
        enterpriseBalanceService.topUp(companyId, body.get("amount"));
    }

    @Operation(summary = "企业流水列表")
    @GetMapping("/transactions")
    public PageVO<EnterpriseTransactionVO> getTransactions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        Long companyId = SecurityUtil.getCurrentCompanyId();
        return enterpriseBalanceService.getTransactions(companyId, page, pageSize);
    }
}
