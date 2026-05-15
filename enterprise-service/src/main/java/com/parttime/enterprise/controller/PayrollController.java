package com.parttime.enterprise.controller;

import com.parttime.enterprise.pojo.cmd.PayrollBatchCmd;
import com.parttime.enterprise.pojo.vo.PayrollBatchVO;
import com.parttime.enterprise.pojo.vo.PayrollItemVO;
import com.parttime.enterprise.service.PayrollService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/payroll")
public class PayrollController {

    private final PayrollService payrollService;

    public PayrollController(PayrollService payrollService) {
        this.payrollService = payrollService;
    }

    @PostMapping("/batches")
    @ResponseStatus(HttpStatus.CREATED)
    public PayrollBatchVO createBatch(@RequestBody PayrollBatchCmd request) {
        return payrollService.createBatch(request);
    }

    @PostMapping("/batches/{id}/calculate")
    public PayrollBatchVO calculateBatch(@PathVariable Long id) {
        return payrollService.calculateBatch(id);
    }

    @PostMapping("/batches/{id}/confirm")
    public PayrollBatchVO confirmBatch(@PathVariable Long id) {
        return payrollService.confirmBatch(id);
    }

    @PostMapping("/batches/{id}/pay")
    public PayrollBatchVO payBatch(@PathVariable Long id) {
        return payrollService.payBatch(id);
    }

    @GetMapping("/batches/{id}")
    public PayrollBatchVO getBatchById(@PathVariable Long id) {
        return payrollService.getBatchById(id);
    }

    @GetMapping("/batches")
    public List<PayrollBatchVO> getBatchesByCompany(@RequestParam Long companyId) {
        return payrollService.getBatchesByCompany(companyId);
    }

    @GetMapping("/batches/{id}/items")
    public List<PayrollItemVO> getBatchItems(@PathVariable Long id) {
        return payrollService.getBatchItems(id);
    }
}
