package com.parttime.platform.controller;

import com.parttime.platform.pojo.cmd.JobCategoryCmd;
import com.parttime.platform.pojo.vo.JobCategoryVO;
import com.parttime.platform.service.JobCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/platform/job-categories")
public class JobCategoryController {

    @Resource
    private JobCategoryService jobCategoryService;

    @Operation(summary = "获取所有岗位分类", description = "获取平台所有岗位分类列表")
    @GetMapping
    public List<JobCategoryVO> getAllCategories() {
        return jobCategoryService.getAllCategories();
    }

    @Operation(summary = "创建岗位分类", description = "创建新的岗位分类")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JobCategoryVO createCategory(@RequestBody JobCategoryCmd cmd) {
        return jobCategoryService.createCategory(cmd);
    }

    @Operation(summary = "更新岗位分类", description = "更新岗位分类信息")
    @PutMapping
    public JobCategoryVO updateCategory(@Parameter(description = "分类ID") @RequestParam Long id, @RequestBody JobCategoryCmd cmd) {
        return jobCategoryService.updateCategory(id, cmd);
    }

    @Operation(summary = "删除岗位分类", description = "删除指定的岗位分类")
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@Parameter(description = "分类ID") @RequestParam Long id) {
        jobCategoryService.deleteCategory(id);
    }
}
