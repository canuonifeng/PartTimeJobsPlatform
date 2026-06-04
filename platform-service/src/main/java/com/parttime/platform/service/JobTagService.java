package com.parttime.platform.service;

import com.parttime.platform.pojo.cmd.JobTagCmd;
import com.parttime.platform.pojo.cmd.JobTagGroupCmd;
import com.parttime.platform.pojo.vo.JobTagGroupVO;
import com.parttime.platform.pojo.vo.JobTagVO;

import java.util.List;

public interface JobTagService {

    List<JobTagGroupVO> getAllGroups();

    JobTagGroupVO createGroup(JobTagGroupCmd cmd);

    JobTagGroupVO updateGroup(Long id, JobTagGroupCmd cmd);

    void deleteGroup(Long id);

    JobTagVO createTag(JobTagCmd cmd);

    JobTagVO updateTag(Long id, JobTagCmd cmd);

    void deleteTag(Long id);
}
