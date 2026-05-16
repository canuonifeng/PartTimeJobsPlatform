package com.parttime.enterprise.controller;

import com.parttime.enterprise.config.SecurityUtil;
import com.parttime.enterprise.pojo.cmd.PayrollBatchCmd;
import com.parttime.enterprise.pojo.vo.PayrollBatchVO;
import com.parttime.enterprise.pojo.vo.PayrollItemVO;
import com.parttime.enterprise.service.PayrollService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import java.util.List;

@RestController
@RequestMapping("/api/payroll")
public class PayrollController {

    @Resource
    private PayrollService payrollService;

    @Operation(summary = "创建薪资批次", description = "创建新的薪资计算批次")
    @PostMapping("/batches")
    @ResponseStatus(HttpStatus.CREATED)
    public PayrollBatchVO createBatch(@RequestBody PayrollBatchCmd request) {
        return payrollService.createBatch(request);
    }

    @Operation(summary = "计算薪资批次", description = "计算指定批次的薪资")
    @PostMapping("/batches/calculate")
    public PayrollBatchVO calculateBatch(@Parameter(description = "批次ID") @RequestParam Long id) {
        return payrollService.calculateBatch(id);
    }

    @Operation(summary = "确认薪资批次", description = "确认指定批次的薪资数据")
    @PostMapping("/batches/confirm")
    public PayrollBatchVO confirmBatch(@Parameter(description = "批次ID") @RequestParam Long id) {
        return payrollService.confirmBatch(id);
    }

    @Operation(summary = "支付薪资批次", description = "支付指定批次的薪资")
    @PostMapping("/batches/pay")
    public PayrollBatchVO payBatch(@Parameter(description = "批次ID") @RequestParam Long id) {
        return payrollService.payBatch(id);
    }

    @Operation(summary = "获取批次详情", description = "根据ID获取薪资批次详情")
    @GetMapping(value = "/batches", params = "id")
    public PayrollBatchVO getBatchById(@Parameter(description = "批次ID") @RequestParam Long id) {
        return payrollService.getBatchById(id);
    }

    @Operation(summary = "获取企业薪资批次列表", description = "根据企业ID获取所有薪资批次")
    @GetMapping("/batches")
    public List<PayrollBatchVO> getBatchesByCompany() {
        Long companyId = SecurityUtil.getCurrentCompanyId();
        return payrollService.getBatchesByCompany(companyId);
    }

    @Operation(summary = "获取批次薪资项", description = "获取指定批次的薪资明细项")
    @GetMapping("/batches/items")
    public List<PayrollItemVO> getBatchItems(@Parameter(description = "批次ID") @RequestParam Long id) {
        return payrollService.getBatchItems(id);
    }
}
