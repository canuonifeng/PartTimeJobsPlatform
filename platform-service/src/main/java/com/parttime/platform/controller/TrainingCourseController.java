package com.parttime.platform.controller;

import com.parttime.platform.pojo.cmd.IdCmd;
import com.parttime.platform.pojo.cmd.TrainingCourseCmd;
import com.parttime.platform.pojo.vo.ApiResponse;
import com.parttime.platform.pojo.vo.TrainingCourseVO;
import com.parttime.platform.service.TrainingCourseService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/admin/training")
public class TrainingCourseController {

    @Resource
    private TrainingCourseService trainingCourseService;

    @Operation(summary = "培训课程列表", description = "获取所有培训课程")
    @GetMapping("/courses")
    public ApiResponse<List<TrainingCourseVO>> list() {
        return ApiResponse.success(trainingCourseService.list());
    }

    @Operation(summary = "创建培训课程", description = "创建新的培训课程（草稿）")
    @PostMapping("/courses")
    public ApiResponse<TrainingCourseVO> create(@RequestBody TrainingCourseCmd cmd) {
        return ApiResponse.success(trainingCourseService.create(cmd));
    }

    @Operation(summary = "更新培训课程", description = "更新培训课程信息")
    @PostMapping("/courses/update")
    public ApiResponse<TrainingCourseVO> update(@RequestBody TrainingCourseCmd cmd) {
        return ApiResponse.success(trainingCourseService.update(cmd));
    }

    @Operation(summary = "发布培训课程", description = "发布课程，对兼职可见")
    @PostMapping("/courses/publish")
    public ApiResponse<Void> publish(@RequestBody IdCmd cmd) {
        trainingCourseService.publish(cmd.getId());
        return ApiResponse.success();
    }

    @Operation(summary = "下线培训课程", description = "下线课程，兼职不可见")
    @PostMapping("/courses/offline")
    public ApiResponse<Void> offline(@RequestBody IdCmd cmd) {
        trainingCourseService.offline(cmd.getId());
        return ApiResponse.success();
    }

    @Operation(summary = "删除培训课程", description = "删除未发布的课程")
    @PostMapping("/courses/delete")
    public ApiResponse<Void> delete(@RequestBody IdCmd cmd) {
        trainingCourseService.delete(cmd.getId());
        return ApiResponse.success();
    }
}
