package com.parttime.platform.controller;

import com.parttime.platform.pojo.cmd.ComplaintArbitrateCmd;
import com.parttime.platform.pojo.cmd.ComplaintHandleCmd;
import com.parttime.platform.pojo.cmd.ComplaintQueryCmd;
import com.parttime.platform.pojo.cmd.IdCmd;
import com.parttime.platform.pojo.vo.ApiResponse;
import com.parttime.platform.pojo.vo.ComplaintVO;
import com.parttime.platform.service.ComplaintService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/complaints")
public class ComplaintController {

    @Resource
    private ComplaintService complaintService;

    @Operation(summary = "投诉工单列表")
    @PostMapping("/list")
    public ApiResponse<List<ComplaintVO>> list(@RequestBody(required = false) ComplaintQueryCmd body) {
        return ApiResponse.success(complaintService.list(body));
    }

    @Operation(summary = "投诉工单详情")
    @PostMapping("/detail")
    public ApiResponse<ComplaintVO> detail(@RequestBody IdCmd body) {
        return ApiResponse.success(complaintService.detail(body.getId()));
    }

    @Operation(summary = "处理投诉工单")
    @PostMapping("/handle")
    public ApiResponse<Void> handle(@RequestBody ComplaintHandleCmd body) {
        complaintService.handle(body);
        return ApiResponse.success();
    }

    @Operation(summary = "仲裁投诉工单")
    @PostMapping("/arbitrate")
    public ApiResponse<Void> arbitrate(@RequestBody ComplaintArbitrateCmd body) {
        complaintService.arbitrate(body);
        return ApiResponse.success();
    }

    @Operation(summary = "关闭投诉工单")
    @PostMapping("/close")
    public ApiResponse<Void> close(@RequestBody IdCmd body) {
        complaintService.close(body.getId());
        return ApiResponse.success();
    }
}
