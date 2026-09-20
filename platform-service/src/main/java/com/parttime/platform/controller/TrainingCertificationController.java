package com.parttime.platform.controller;

import com.parttime.platform.pojo.cmd.IdCmd;
import com.parttime.platform.pojo.cmd.ToggleStatusCmd;
import com.parttime.platform.pojo.cmd.TrainingCertificationCmd;
import com.parttime.platform.pojo.vo.ApiResponse;
import com.parttime.platform.pojo.vo.TrainingCertificationVO;
import com.parttime.platform.service.TrainingCertificationService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/admin/training")
public class TrainingCertificationController {

    @Resource
    private TrainingCertificationService trainingCertificationService;

    @Operation(summary = "技能认证列表", description = "获取所有技能认证")
    @GetMapping("/certifications")
    public ApiResponse<List<TrainingCertificationVO>> list() {
        return ApiResponse.success(trainingCertificationService.list());
    }

    @Operation(summary = "创建技能认证", description = "创建新的技能认证")
    @PostMapping("/certifications")
    public ApiResponse<TrainingCertificationVO> create(@RequestBody TrainingCertificationCmd cmd) {
        return ApiResponse.success(trainingCertificationService.create(cmd));
    }

    @Operation(summary = "更新技能认证", description = "更新技能认证信息")
    @PostMapping("/certifications/update")
    public ApiResponse<TrainingCertificationVO> update(@RequestBody TrainingCertificationCmd cmd) {
        return ApiResponse.success(trainingCertificationService.update(cmd));
    }

    @Operation(summary = "启用/停用技能认证", description = "status: ACTIVE 启用，DISABLED 停用")
    @PostMapping("/certifications/toggle")
    public ApiResponse<Void> toggle(@RequestBody ToggleStatusCmd cmd) {
        trainingCertificationService.toggle(cmd.getId(), cmd.getStatus());
        return ApiResponse.success();
    }
}
