package com.parttime.platform.mapper;

import com.parttime.platform.pojo.entity.JobTag;
import com.parttime.platform.pojo.entity.JobTagGroup;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface JobTagMapper {

    int insertGroup(JobTagGroup group);

    Optional<JobTagGroup> findGroupById(Long id);

    List<JobTagGroup> findAllGroups();

    int updateGroup(JobTagGroup group);

    int disableGroup(Long id);

    int disableTagsByGroupId(Long groupId);

    int insertTag(JobTag tag);

    Optional<JobTag> findTagById(Long id);

    List<JobTag> findAllTags();

    int updateTag(JobTag tag);

    int disableTag(Long id);
}
