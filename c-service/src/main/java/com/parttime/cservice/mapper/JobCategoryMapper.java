package com.parttime.cservice.mapper;

import com.parttime.cservice.pojo.entity.JobCategory;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface JobCategoryMapper {
    List<JobCategory> findActive();
}
