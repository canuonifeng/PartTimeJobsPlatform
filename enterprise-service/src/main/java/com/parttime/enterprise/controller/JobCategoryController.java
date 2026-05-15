package com.parttime.enterprise.controller;

import com.parttime.enterprise.pojo.cmd.JobCategoryCmd;
import com.parttime.enterprise.pojo.vo.JobCategoryVO;
import com.parttime.enterprise.service.JobCategoryService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/job-categories")
public class JobCategoryController {

    private final JobCategoryService jobCategoryService;

    public JobCategoryController(JobCategoryService jobCategoryService) {
        this.jobCategoryService = jobCategoryService;
    }

    @GetMapping
    public List<JobCategoryVO> getAllCategories() {
        return jobCategoryService.getAllCategories();
    }

    @GetMapping("/{id}")
    public JobCategoryVO getCategoryById(@PathVariable Long id) {
        return jobCategoryService.getCategoryById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JobCategoryVO createCategory(@RequestBody JobCategoryCmd request) {
        return jobCategoryService.createCategory(request);
    }

    @PutMapping("/{id}")
    public JobCategoryVO updateCategory(@PathVariable Long id, @RequestBody JobCategoryCmd request) {
        return jobCategoryService.updateCategory(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long id) {
        jobCategoryService.deleteCategory(id);
    }
}
