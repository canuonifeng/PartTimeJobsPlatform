package com.parttime.cservice.mapper;

import com.parttime.cservice.pojo.entity.ApplicationSchedule;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface ApplicationScheduleMapper {
    void insert(ApplicationSchedule record);
    List<ApplicationSchedule> findByApplicationId(Long applicationId);
    List<Long> findScheduleIdsByJobAndWorker(@Param("jobId") Long jobId, @Param("workerId") Long workerId);
}
