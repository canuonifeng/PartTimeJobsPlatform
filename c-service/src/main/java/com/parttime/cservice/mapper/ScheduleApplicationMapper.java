package com.parttime.cservice.mapper;

import com.parttime.cservice.pojo.entity.ScheduleApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ScheduleApplicationMapper {
    int insert(ScheduleApplication app);
    Optional<ScheduleApplication> findByScheduleIdAndWorkerId(@Param("scheduleId") Long scheduleId, @Param("workerId") Long workerId);
    List<ScheduleApplication> findByWorkerId(Long workerId);
    List<Long> findScheduleIdsByWorkerIdAndJobId(@Param("workerId") Long workerId, @Param("jobId") Long jobId);
    int countByScheduleId(Long scheduleId);
}
