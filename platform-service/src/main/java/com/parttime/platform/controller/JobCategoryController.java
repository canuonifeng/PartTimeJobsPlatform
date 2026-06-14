package com.parttime.platform.controller;

import com.parttime.platform.pojo.cmd.IdCmd;
import com.parttime.platform.pojo.cmd.JobCategoryCmd;
import com.parttime.platform.pojo.vo.ApiResponse;
import com.parttime.platform.pojo.vo.JobCategoryVO;
import com.parttime.platform.service.JobCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/admin/job-categories")
public class JobCategoryController {

    @Resource
    private JobCategoryService jobCategoryService;

    @Operation(summary = "获取所有岗位分类", description = "获取平台所有岗位分类列表")
    @GetMapping
    public ApiResponse<List<JobCategoryVO>> getAllCategories() {
        return ApiResponse.success(jobCategoryService.getAllCategories());
    }

    @Operation(summary = "创建岗位分类", description = "创建新的岗位分类")
    @PostMapping
    public ApiResponse<JobCategoryVO> createCategory(@RequestBody JobCategoryCmd cmd) {
        return ApiResponse.success(jobCategoryService.createCategory(cmd));
    }

    @Operation(summary = "更新岗位分类", description = "更新岗位分类信息")
    @PostMapping("/update")
    public ApiResponse<JobCategoryVO> updateCategory(@RequestBody JobCategoryCmd cmd) {
        return ApiResponse.success(jobCategoryService.updateCategory(cmd.getId(), cmd));
    }

    @Operation(summary = "删除岗位分类", description = "删除指定的岗位分类")
    @PostMapping("/delete")
    public void deleteCategory(@RequestBody IdCmd cmd) {
        jobCategoryService.deleteCategory(cmd.getId());
    }
}
