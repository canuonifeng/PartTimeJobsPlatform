package com.parttime.enterprise.mapper;

import com.parttime.enterprise.pojo.entity.JobCategory;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface JobCategoryMapper {

    int insert(JobCategory category);

    Optional<JobCategory> findById(Long id);

    List<JobCategory> findAll();

    int update(JobCategory category);

    int delete(Long id);
}
