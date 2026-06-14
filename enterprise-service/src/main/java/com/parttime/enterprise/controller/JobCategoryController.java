package com.parttime.enterprise.controller;

import com.parttime.enterprise.pojo.cmd.IdCmd;
import com.parttime.enterprise.pojo.cmd.JobCategoryCmd;
import com.parttime.enterprise.pojo.vo.ApiResponse;
import com.parttime.enterprise.pojo.vo.JobCategoryVO;
import com.parttime.enterprise.service.JobCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import java.util.List;

@RestController
@RequestMapping("/api/enterprise/job-categories")
public class JobCategoryController {

    @Resource
    private JobCategoryService jobCategoryService;

    @Operation(summary = "获取所有岗位分类", description = "获取所有岗位分类列表，包含树形结构")
    @GetMapping
    public ApiResponse<List<JobCategoryVO>> getAllCategories() {
        return ApiResponse.success(jobCategoryService.getAllCategories());
    }

    @Operation(summary = "获取分类详情", description = "根据ID获取岗位分类详情")
    @GetMapping(params = "id")
    public ApiResponse<JobCategoryVO> getCategoryById(@Parameter(description = "分类ID") @RequestParam Long id) {
        return ApiResponse.success(jobCategoryService.getCategoryById(id));
    }

    @Operation(summary = "创建岗位分类", description = "创建新的岗位分类")
    @PostMapping
    public ApiResponse<JobCategoryVO> createCategory(@RequestBody JobCategoryCmd request) {
        return ApiResponse.success(jobCategoryService.createCategory(request));
    }

    @Operation(summary = "更新岗位分类", description = "更新岗位分类信息")
    @PostMapping("/update")
    public ApiResponse<JobCategoryVO> updateCategory(@RequestBody JobCategoryCmd request) {
        return ApiResponse.success(jobCategoryService.updateCategory(request.getId(), request));
    }

    @Operation(summary = "删除岗位分类", description = "删除指定的岗位分类")
    @PostMapping("/delete")
    public void deleteCategory(@RequestBody IdCmd cmd) {
        jobCategoryService.deleteCategory(cmd.getId());
    }
}
