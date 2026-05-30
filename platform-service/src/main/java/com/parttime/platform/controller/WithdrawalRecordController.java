package com.parttime.platform.controller;

import com.parttime.platform.pojo.vo.PageVO;
import com.parttime.platform.pojo.vo.WithdrawalRecordVO;
import com.parttime.platform.service.WithdrawalRecordService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/withdrawals")
public class WithdrawalRecordController {

    @Resource
    private WithdrawalRecordService withdrawalRecordService;

    @Operation(summary = "运营后台提现记录列表")
    @GetMapping
    public PageVO<WithdrawalRecordVO> listRecords(@RequestParam(required = false) Long workerId,
                                                  @RequestParam(required = false) String status,
                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
                                                  @RequestParam(defaultValue = "1") Integer page,
                                                  @RequestParam(defaultValue = "20") Integer pageSize) {
        return withdrawalRecordService.listRecords(workerId, status, startTime, endTime, page, pageSize);
    }
}
