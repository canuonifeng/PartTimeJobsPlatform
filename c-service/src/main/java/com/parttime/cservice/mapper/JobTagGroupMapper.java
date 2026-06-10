package com.parttime.cservice.mapper;

import com.parttime.cservice.pojo.entity.JobTagGroup;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface JobTagGroupMapper {

    List<JobTagGroup> findActiveGroups();
}
