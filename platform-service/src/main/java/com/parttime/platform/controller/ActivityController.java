package com.parttime.platform.controller;

import com.parttime.platform.pojo.cmd.ActivityCmd;
import com.parttime.platform.pojo.cmd.ActivityToggleCmd;
import com.parttime.platform.pojo.cmd.IdCmd;
import com.parttime.platform.pojo.cmd.PushTaskCmd;
import com.parttime.platform.pojo.vo.ActivityEffectVO;
import com.parttime.platform.pojo.vo.ApiResponse;
import com.parttime.platform.pojo.vo.OperationActivityVO;
import com.parttime.platform.pojo.vo.PushTaskVO;
import com.parttime.platform.service.ActivityService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/activities")
public class ActivityController {

    @Resource
    private ActivityService activityService;

    @Operation(summary = "获取活动列表")
    @PostMapping("/list")
    public ApiResponse<List<OperationActivityVO>> list(@RequestBody(required = false) IdCmd body) {
        return ApiResponse.success(activityService.list());
    }

    @Operation(summary = "创建活动")
    @PostMapping("/create")
    public ApiResponse<Void> create(@RequestBody ActivityCmd body) {
        activityService.create(body);
        return ApiResponse.success();
    }

    @Operation(summary = "更新活动")
    @PostMapping("/update")
    public ApiResponse<Void> update(@RequestBody ActivityCmd body) {
        activityService.update(body);
        return ApiResponse.success();
    }

    @Operation(summary = "切换活动状态")
    @PostMapping("/toggle")
    public ApiResponse<Void> toggle(@RequestBody ActivityToggleCmd body) {
        activityService.toggle(body);
        return ApiResponse.success();
    }

    @Operation(summary = "删除活动")
    @PostMapping("/delete")
    public ApiResponse<Void> delete(@RequestBody IdCmd body) {
        activityService.delete(body);
        return ApiResponse.success();
    }

    @Operation(summary = "获取活动效果统计")
    @PostMapping("/effect-stats")
    public ApiResponse<ActivityEffectVO> effectStats(@RequestBody IdCmd body) {
        return ApiResponse.success(activityService.effectStats(body));
    }

    @Operation(summary = "获取推送任务列表")
    @PostMapping("/push-tasks")
    public ApiResponse<List<PushTaskVO>> pushTasks(@RequestBody(required = false) IdCmd body) {
        return ApiResponse.success(activityService.pushTasks());
    }

    @Operation(summary = "创建推送任务")
    @PostMapping("/push/create")
    public ApiResponse<Void> createPush(@RequestBody PushTaskCmd body) {
        activityService.createPush(body);
        return ApiResponse.success();
    }
}
