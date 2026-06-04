package com.parttime.enterprise.service;

import com.parttime.enterprise.pojo.vo.JobTagGroupVO;

import java.util.List;

public interface JobTagService {

    List<JobTagGroupVO> getActiveGroups();
}
