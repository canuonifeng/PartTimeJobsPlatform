package com.parttime.platform.mapper;

import com.parttime.platform.pojo.entity.JobCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface JobCategoryMapper {

    int insert(JobCategory category);

    Optional<JobCategory> findById(Long id);

    List<JobCategory> findAll();

    List<JobCategory> findByParentId(Long parentId);

    int update(JobCategory category);

    int delete(Long id);
}
