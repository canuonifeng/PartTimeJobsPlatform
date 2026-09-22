package com.parttime.cservice.controller;

import com.parttime.cservice.pojo.cmd.ExamSubmitCmd;
import com.parttime.cservice.pojo.cmd.LessonCompleteCmd;
import com.parttime.cservice.pojo.cmd.LessonExamSubmitCmd;
import com.parttime.cservice.pojo.cmd.LessonProgressCmd;
import com.parttime.cservice.pojo.cmd.StartCourseCmd;
import com.parttime.cservice.pojo.cmd.StartLessonCmd;
import com.parttime.cservice.pojo.vo.ApiResponse;
import com.parttime.cservice.pojo.vo.ExamResultVO;
import com.parttime.cservice.pojo.vo.LessonExamResultVO;
import com.parttime.cservice.pojo.vo.LessonStartVO;
import com.parttime.cservice.pojo.vo.TrainingCourseDetailVO;
import com.parttime.cservice.pojo.vo.TrainingCourseVO;
import com.parttime.cservice.pojo.vo.WorkerCertificationVO;
import com.parttime.cservice.service.TrainingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/worker/training")
public class TrainingController {

    @Resource
    private TrainingService trainingService;

    @Operation(summary = "培训课程列表", description = "获取已发布的培训课程及我的学习状态")
    @GetMapping("/courses")
    public ApiResponse<List<TrainingCourseVO>> listCourses() {
        Long workerId = getWorkerId();
        return ApiResponse.success(trainingService.listCourses(workerId));
    }

    @Operation(summary = "培训课程详情", description = "获取课程内容、考试题目（不含答案）及我的学习状态")
    @GetMapping("/courses/detail")
    public ApiResponse<TrainingCourseDetailVO> getCourseDetail(@Parameter(description = "课程ID") @RequestParam Long id) {
        Long workerId = getWorkerId();
        return ApiResponse.success(trainingService.getCourseDetail(workerId, id));
    }

    @Operation(summary = "开始学习课时", description = "校验前置课时后创建学习记录并返回课时内容")
    @PostMapping("/lessons/start")
    public ApiResponse<LessonStartVO> startLesson(@RequestBody StartLessonCmd cmd) {
        Long workerId = requireWorkerId();
        return ApiResponse.success(trainingService.startLesson(workerId, cmd));
    }

    @Operation(summary = "上报课时进度", description = "音视频课时上报播放进度，达100%自动完成")
    @PostMapping("/lessons/progress")
    public ApiResponse<Void> reportProgress(@RequestBody LessonProgressCmd cmd) {
        Long workerId = requireWorkerId();
        trainingService.reportProgress(workerId, cmd);
        return ApiResponse.success();
    }

    @Operation(summary = "标记课时已完成", description = "文档/图文课时标记已读")
    @PostMapping("/lessons/complete")
    public ApiResponse<Void> markComplete(@RequestBody LessonCompleteCmd cmd) {
        Long workerId = requireWorkerId();
        trainingService.markComplete(workerId, cmd);
        return ApiResponse.success();
    }

    @Operation(summary = "提交课时考试", description = "判分、记录成绩与快照，通过则完成课时")
    @PostMapping("/lessons/exam/submit")
    public ApiResponse<LessonExamResultVO> submitLessonExam(@RequestBody LessonExamSubmitCmd cmd) {
        Long workerId = requireWorkerId();
        return ApiResponse.success(trainingService.submitLessonExam(workerId, cmd));
    }

    @Operation(summary = "开始学习", description = "开始学习课程，创建学习记录")
    @PostMapping("/courses/start")
    public ApiResponse<Void> startCourse(@RequestBody StartCourseCmd cmd) {
        Long workerId = requireWorkerId();
        trainingService.startCourse(workerId, cmd.getCourseId());
        return ApiResponse.success();
    }

    @Operation(summary = "提交考试", description = "提交考试答案，判分并发放认证")
    @PostMapping("/courses/exam")
    public ApiResponse<ExamResultVO> submitExam(@RequestBody ExamSubmitCmd cmd) {
        Long workerId = requireWorkerId();
        return ApiResponse.success(trainingService.submitExam(workerId, cmd.getCourseId(), cmd));
    }

    @Operation(summary = "我的技能认证", description = "获取当前兼职已获得的技能认证列表")
    @GetMapping("/certifications/my")
    public ApiResponse<List<WorkerCertificationVO>> getMyCertifications() {
        Long workerId = requireWorkerId();
        return ApiResponse.success(trainingService.getMyCertifications(workerId));
    }

    private Long getWorkerId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return Long.valueOf(auth.getName());
    }

    private Long requireWorkerId() {
        Long workerId = getWorkerId();
        if (workerId == null) {
            throw new RuntimeException("未登录");
        }
        return workerId;
    }
}
