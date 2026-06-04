package com.parttime.enterprise.mapper;

import com.parttime.enterprise.pojo.entity.JobTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface JobTagRelationMapper {

    int deleteByJobId(Long jobId);

    int insert(@Param("jobId") Long jobId, @Param("tagId") Long tagId);

    List<Long> findTagIdsByJobId(Long jobId);

    List<JobTag> findTagsByJobId(Long jobId);
}
