package com.parttime.enterprise.mapper;

import com.parttime.enterprise.pojo.entity.JobTag;
import com.parttime.enterprise.pojo.entity.JobTagGroup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface JobTagMapper {

    List<JobTagGroup> findActiveGroups();

    List<JobTag> findActiveTags();

    List<Long> findActiveExistingIds(@Param("ids") List<Long> ids);
}
