package com.parttime.cservice.mapper;

import com.parttime.cservice.pojo.entity.WorkerProfile;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface WorkerProfileMapper {
    int insert(WorkerProfile profile);
    Optional<WorkerProfile> findByWorkerId(Long workerId);
    int update(WorkerProfile profile);
}
