package com.parttime.platform.controller;

import com.parttime.platform.pojo.vo.PageVO;
import com.parttime.platform.pojo.vo.SettlementBillVO;
import com.parttime.platform.service.SettlementBillService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/settlement")
public class SettlementBillController {

    @Resource
    private SettlementBillService settlementBillService;

    @Operation(summary = "运营后台结算账单列表")
    @GetMapping("/bills")
    public PageVO<SettlementBillVO> listBills(@RequestParam(required = false) Long companyId,
                                              @RequestParam(required = false) String workerName,
                                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
                                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
                                              @RequestParam(required = false) String status,
                                              @RequestParam(defaultValue = "1") Integer page,
                                              @RequestParam(defaultValue = "20") Integer pageSize) {
        return settlementBillService.listBills(companyId, workerName, dateFrom, dateTo, status, page, pageSize);
    }
}
