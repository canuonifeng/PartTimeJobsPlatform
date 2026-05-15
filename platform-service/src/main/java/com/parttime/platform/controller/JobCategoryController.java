package com.parttime.platform.controller;

import com.parttime.platform.pojo.cmd.JobCategoryCmd;
import com.parttime.platform.pojo.vo.JobCategoryVO;
import com.parttime.platform.service.JobCategoryService;
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
@RequestMapping("/api/admin/job-categories")
public class JobCategoryController {

    @Resource
    private JobCategoryService jobCategoryService;

    @GetMapping
    public List<JobCategoryVO> getAllCategories() {
        return jobCategoryService.getAllCategories();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JobCategoryVO createCategory(@RequestBody JobCategoryCmd cmd) {
        return jobCategoryService.createCategory(cmd);
    }

    @PutMapping
    public JobCategoryVO updateCategory(@RequestParam Long id, @RequestBody JobCategoryCmd cmd) {
        return jobCategoryService.updateCategory(id, cmd);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@RequestParam Long id) {
        jobCategoryService.deleteCategory(id);
    }
}
