package com.parttime.cservice.mapper;

import com.parttime.cservice.pojo.vo.JobTagVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface JobTagRelationMapper {
    List<JobTagVO> findTagsByJobId(Long jobId);

    List<JobTagVO> findTagsByJobIds(List<Long> jobIds);
}
