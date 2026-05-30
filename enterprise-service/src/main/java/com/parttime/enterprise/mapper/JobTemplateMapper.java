package com.parttime.enterprise.mapper;

import com.parttime.enterprise.pojo.entity.JobTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface JobTemplateMapper {
    List<JobTemplate> findByCompanyId(@Param("companyId") Long companyId);
    Optional<JobTemplate> findById(@Param("id") Long id);
    int insert(JobTemplate template);
    int update(JobTemplate template);
    int deleteById(@Param("id") Long id);
}
