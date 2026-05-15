package com.parttime.enterprise.web.controller;

import com.parttime.enterprise.api.dto.PayrollBatchRequest;
import com.parttime.enterprise.api.dto.PayrollBatchResponse;
import com.parttime.enterprise.api.dto.PayrollItemResponse;
import com.parttime.enterprise.core.service.PayrollService;

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
    public PayrollBatchResponse createBatch(@RequestBody PayrollBatchRequest request) {
        return payrollService.createBatch(request);
    }

    @PostMapping("/batches/{id}/calculate")
    public PayrollBatchResponse calculateBatch(@PathVariable Long id) {
        return payrollService.calculateBatch(id);
    }

    @PostMapping("/batches/{id}/confirm")
    public PayrollBatchResponse confirmBatch(@PathVariable Long id) {
        return payrollService.confirmBatch(id);
    }

    @PostMapping("/batches/{id}/pay")
    public PayrollBatchResponse payBatch(@PathVariable Long id) {
        return payrollService.payBatch(id);
    }

    @GetMapping("/batches/{id}")
    public PayrollBatchResponse getBatchById(@PathVariable Long id) {
        return payrollService.getBatchById(id);
    }

    @GetMapping("/batches")
    public List<PayrollBatchResponse> getBatchesByCompany(@RequestParam Long companyId) {
        return payrollService.getBatchesByCompany(companyId);
    }

    @GetMapping("/batches/{id}/items")
    public List<PayrollItemResponse> getBatchItems(@PathVariable Long id) {
        return payrollService.getBatchItems(id);
    }
}
