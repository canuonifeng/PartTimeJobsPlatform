package com.parttime.enterprise.controller;

import com.parttime.enterprise.pojo.vo.ApiResponse;
import com.parttime.enterprise.pojo.vo.JobTagGroupVO;
import com.parttime.enterprise.service.JobTagService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import java.util.List;

@RestController
@RequestMapping("/api/enterprise/job-tags")
public class JobTagController {

    @Resource
    private JobTagService jobTagService;

    @Operation(summary = "获取启用岗位标签")
    @GetMapping
    public ApiResponse<List<JobTagGroupVO>> getActiveTags() {
        return ApiResponse.success(jobTagService.getActiveGroups());
    }
}
