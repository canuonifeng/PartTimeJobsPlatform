package com.parttime.platform.controller;

import com.parttime.platform.pojo.cmd.AttendanceReviewCmd;
import com.parttime.platform.pojo.cmd.IdCmd;
import com.parttime.platform.pojo.cmd.JobIdCmd;
import com.parttime.platform.pojo.cmd.JobQueryCmd;
import com.parttime.platform.pojo.vo.ApiResponse;
import com.parttime.platform.pojo.vo.AttendanceRecordVO;
import com.parttime.platform.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/attendance")
public class AttendanceController {

    @Resource
    private AttendanceService attendanceService;

    @Operation(summary = "获取考勤列表")
    @PostMapping("/list")
    public ApiResponse<List<AttendanceRecordVO>> list(@RequestBody(required = false) JobQueryCmd body) {
        JobQueryCmd cmd = body != null ? body : new JobQueryCmd();
        return ApiResponse.success(attendanceService.list(cmd));
    }

    @Operation(summary = "按职位获取考勤列表")
    @PostMapping("/list-by-job")
    public ApiResponse<List<AttendanceRecordVO>> listByJobId(@RequestBody JobIdCmd body) {
        return ApiResponse.success(attendanceService.listByJobId(body.getJobId()));
    }

    @Operation(summary = "获取考勤详情")
    @PostMapping("/detail")
    public ApiResponse<AttendanceRecordVO> detail(@RequestBody IdCmd body) {
        return ApiResponse.success(attendanceService.detail(body.getId()));
    }

    @Operation(summary = "异常审核/补卡审批")
    @PostMapping("/update-status")
    public ApiResponse<Void> updateStatus(@RequestBody AttendanceReviewCmd body) {
        attendanceService.updateStatus(body.getId(), body.getStatus(), body.getRemark());
        return ApiResponse.success();
    }
}
