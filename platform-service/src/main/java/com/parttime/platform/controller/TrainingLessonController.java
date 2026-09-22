package com.parttime.platform.controller;

import com.parttime.platform.pojo.cmd.IdCmd;
import com.parttime.platform.pojo.cmd.TrainingLessonCreateCmd;
import com.parttime.platform.pojo.cmd.TrainingLessonQueryCmd;
import com.parttime.platform.pojo.cmd.TrainingLessonUpdateCmd;
import com.parttime.platform.pojo.vo.ApiResponse;
import com.parttime.platform.pojo.vo.TrainingLessonVO;
import com.parttime.platform.service.TrainingLessonService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/admin/training/lessons")
public class TrainingLessonController {

    @Resource
    private TrainingLessonService trainingLessonService;

    @Operation(summary = "课时列表", description = "按课程ID查询课时列表")
    @PostMapping("/list")
    public ApiResponse<List<TrainingLessonVO>> list(@RequestBody TrainingLessonQueryCmd cmd) {
        return ApiResponse.success(trainingLessonService.list(cmd));
    }

    @Operation(summary = "创建课时", description = "创建新课时（草稿）")
    @PostMapping("/create")
    public ApiResponse<TrainingLessonVO> create(@RequestBody TrainingLessonCreateCmd cmd) {
        return ApiResponse.success(trainingLessonService.create(cmd));
    }

    @Operation(summary = "更新课时", description = "更新课时信息")
    @PostMapping("/update")
    public ApiResponse<TrainingLessonVO> update(@RequestBody TrainingLessonUpdateCmd cmd) {
        return ApiResponse.success(trainingLessonService.update(cmd));
    }

    @Operation(summary = "删除课时", description = "删除草稿或已下线的课时")
    @PostMapping("/delete")
    public ApiResponse<Void> delete(@RequestBody IdCmd cmd) {
        trainingLessonService.delete(cmd.getId());
        return ApiResponse.success();
    }

    @Operation(summary = "发布课时", description = "草稿课时发布为正式")
    @PostMapping("/publish")
    public ApiResponse<Void> publish(@RequestBody IdCmd cmd) {
        trainingLessonService.publish(cmd.getId());
        return ApiResponse.success();
    }

    @Operation(summary = "下线课时", description = "下线已发布课时")
    @PostMapping("/offline")
    public ApiResponse<Void> offline(@RequestBody IdCmd cmd) {
        trainingLessonService.offline(cmd.getId());
        return ApiResponse.success();
    }
}
