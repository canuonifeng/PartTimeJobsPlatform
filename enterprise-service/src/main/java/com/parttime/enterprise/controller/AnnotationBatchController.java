package com.parttime.enterprise.controller;

import com.parttime.enterprise.pojo.cmd.AnnotationBatchCreateCmd;
import com.parttime.enterprise.pojo.cmd.AnnotationBatchListCmd;
import com.parttime.enterprise.pojo.cmd.AnnotationBatchToggleCmd;
import com.parttime.enterprise.pojo.cmd.AnnotationBatchUpdateCmd;
import com.parttime.enterprise.pojo.vo.AnnotationBatchVO;
import com.parttime.enterprise.pojo.vo.ApiResponse;
import com.parttime.enterprise.service.AnnotationBatchService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/enterprise/annotation-batches")
public class AnnotationBatchController {

    @Resource
    private AnnotationBatchService annotationBatchService;

    @Operation(summary = "标注批次列表", description = "按岗位ID查询标注批次及进度")
    @PostMapping("/list")
    public ApiResponse<List<AnnotationBatchVO>> list(@RequestBody AnnotationBatchListCmd cmd) {
        return ApiResponse.success(annotationBatchService.list(cmd));
    }

    @Operation(summary = "创建标注批次")
    @PostMapping("/create")
    public ApiResponse<AnnotationBatchVO> create(@RequestBody AnnotationBatchCreateCmd cmd) {
        return ApiResponse.success(annotationBatchService.create(cmd));
    }

    @Operation(summary = "更新标注批次")
    @PostMapping("/update")
    public ApiResponse<AnnotationBatchVO> update(@RequestBody AnnotationBatchUpdateCmd cmd) {
        return ApiResponse.success(annotationBatchService.update(cmd));
    }

    @Operation(summary = "切换标注批次状态")
    @PostMapping("/toggle")
    public ApiResponse<AnnotationBatchVO> toggle(@RequestBody AnnotationBatchToggleCmd cmd) {
        return ApiResponse.success(annotationBatchService.toggle(cmd));
    }
}
