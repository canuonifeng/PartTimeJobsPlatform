package com.parttime.cservice.service;

import com.parttime.cservice.pojo.vo.JobCategoryVO;

import java.util.List;

public interface JobCategoryService {
    List<JobCategoryVO> getActiveCategoryTree();
}
