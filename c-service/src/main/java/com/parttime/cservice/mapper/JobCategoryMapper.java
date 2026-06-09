package com.parttime.cservice.mapper;

import com.parttime.cservice.pojo.entity.JobCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface JobCategoryMapper {
    JobCategory findById(Long id);
    List<JobCategory> findActive();
    List<JobCategory> findByIds(@Param("ids") List<Long> ids);
    int insert(JobCategory category);
}
