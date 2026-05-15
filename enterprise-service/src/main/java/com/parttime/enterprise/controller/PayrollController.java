package com.parttime.enterprise.controller;

import com.parttime.enterprise.pojo.cmd.PayrollBatchCmd;
import com.parttime.enterprise.pojo.vo.PayrollBatchVO;
import com.parttime.enterprise.pojo.vo.PayrollItemVO;
import com.parttime.enterprise.service.PayrollService;

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

    @PostMapping("/batches")
    @ResponseStatus(HttpStatus.CREATED)
    public PayrollBatchVO createBatch(@RequestBody PayrollBatchCmd request) {
        return payrollService.createBatch(request);
    }

    @PostMapping("/batches/calculate")
    public PayrollBatchVO calculateBatch(@RequestParam Long id) {
        return payrollService.calculateBatch(id);
    }

    @PostMapping("/batches/confirm")
    public PayrollBatchVO confirmBatch(@RequestParam Long id) {
        return payrollService.confirmBatch(id);
    }

    @PostMapping("/batches/pay")
    public PayrollBatchVO payBatch(@RequestParam Long id) {
        return payrollService.payBatch(id);
    }

    @GetMapping(value = "/batches", params = "id")
    public PayrollBatchVO getBatchById(@RequestParam Long id) {
        return payrollService.getBatchById(id);
    }

    @GetMapping("/batches")
    public List<PayrollBatchVO> getBatchesByCompany(@RequestParam Long companyId) {
        return payrollService.getBatchesByCompany(companyId);
    }

    @GetMapping("/batches/items")
    public List<PayrollItemVO> getBatchItems(@RequestParam Long id) {
        return payrollService.getBatchItems(id);
    }
}
