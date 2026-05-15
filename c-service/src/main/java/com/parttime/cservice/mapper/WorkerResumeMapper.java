package com.parttime.cservice.mapper;

import com.parttime.cservice.pojo.entity.WorkerResume;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface WorkerResumeMapper {
    int insert(WorkerResume resume);
    List<WorkerResume> findByWorkerId(Long workerId);
    int deleteById(Long id);
}
