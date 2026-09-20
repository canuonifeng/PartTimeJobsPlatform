package com.parttime.enterprise.controller;

import com.parttime.enterprise.pojo.cmd.AnnotationSubmitCmd;
import com.parttime.enterprise.pojo.cmd.ProgressCmd;
import com.parttime.enterprise.pojo.cmd.QualityCheckCmd;
import com.parttime.enterprise.pojo.vo.ApiResponse;
import com.parttime.enterprise.service.ExternalCallbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/enterprise/external")
@Tag(name = "外部系统回调接口", description = "供外部系统调用的标注任务回调接口")
public class ExternalCallbackController {

    @Resource
    private ExternalCallbackService externalCallbackService;

    @Operation(summary = "标注提交回调", description = "外部系统提交标注完成结果")
    @PostMapping("/annotation/submit")
    public ApiResponse<Void> annotationSubmit(@RequestBody AnnotationSubmitCmd cmd,
                                              HttpServletRequest request) {
        String callbackKey = request.getHeader("X-Callback-Key");
        externalCallbackService.handleAnnotationSubmit(cmd, callbackKey);
        return ApiResponse.success();
    }

    @Operation(summary = "质量检查回调", description = "外部系统返回质量检查结果")
    @PostMapping("/annotation/quality-check")
    public ApiResponse<Void> qualityCheck(@RequestBody QualityCheckCmd cmd,
                                          HttpServletRequest request) {
        String callbackKey = request.getHeader("X-Callback-Key");
        externalCallbackService.handleQualityCheck(cmd, callbackKey);
        return ApiResponse.success();
    }

    @Operation(summary = "进度回调", description = "外部系统更新标注进度")
    @PostMapping("/annotation/progress")
    public ApiResponse<Void> progress(@RequestBody ProgressCmd cmd,
                                      HttpServletRequest request) {
        String callbackKey = request.getHeader("X-Callback-Key");
        externalCallbackService.handleProgress(cmd, callbackKey);
        return ApiResponse.success();
    }
}
