package com.parttime.platform.controller;

import com.parttime.platform.pojo.cmd.LeadCreateCmd;
import com.parttime.platform.pojo.vo.ApiResponse;
import com.parttime.platform.pojo.vo.LeadVO;
import com.parttime.platform.service.LeadService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/api/leads")
public class LeadController {

    @Resource
    private LeadService leadService;

    @Operation(summary = "提交官网留资", description = "官网客户提交联系方式后，由客服人员跟进演示")
    @PostMapping
    public ApiResponse<LeadVO> createLead(@RequestBody LeadCreateCmd cmd) {
        return ApiResponse.success(leadService.createLead(cmd));
    }
}
