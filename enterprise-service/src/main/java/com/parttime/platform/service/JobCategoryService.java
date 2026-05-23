package com.parttime.platform.service;

import com.parttime.platform.pojo.cmd.JobCategoryCmd;
import com.parttime.platform.pojo.vo.JobCategoryVO;

import java.util.List;

public interface JobCategoryService {

    List<JobCategoryVO> getAllCategories();

    JobCategoryVO createCategory(JobCategoryCmd cmd);

    JobCategoryVO updateCategory(Long id, JobCategoryCmd cmd);

    void deleteCategory(Long id);
}
