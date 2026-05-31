package com.parttime.cservice.mapper;

import com.parttime.cservice.pojo.entity.WorkerRealNameAuth;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface WorkerRealNameAuthMapper {
    int insert(WorkerRealNameAuth auth);
    int update(WorkerRealNameAuth auth);
    Optional<WorkerRealNameAuth> findByWorkerId(Long workerId);
}
