package com.parttime.cservice.controller;

import com.parttime.cservice.pojo.vo.ApiResponse;
import com.parttime.cservice.pojo.vo.JobCategoryVO;
import com.parttime.cservice.service.JobCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/job-categories")
public class JobCategoryController {

    @Resource
    private JobCategoryService jobCategoryService;

    @Operation(summary = "获取岗位分类", description = "获取启用状态的岗位分类树")
    @GetMapping
    public ApiResponse<List<JobCategoryVO>> getJobCategories() {
        return ApiResponse.success(jobCategoryService.getActiveCategoryTree());
    }
}
