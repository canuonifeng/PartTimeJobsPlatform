package com.parttime.enterprise.service;

import com.parttime.enterprise.pojo.cmd.JobCategoryCmd;
import com.parttime.enterprise.pojo.vo.JobCategoryVO;

import java.util.List;

public interface JobCategoryService {

    List<JobCategoryVO> getAllCategories();

    JobCategoryVO getCategoryById(Long id);

    JobCategoryVO createCategory(JobCategoryCmd request);

    JobCategoryVO updateCategory(Long id, JobCategoryCmd request);

    void deleteCategory(Long id);
}
