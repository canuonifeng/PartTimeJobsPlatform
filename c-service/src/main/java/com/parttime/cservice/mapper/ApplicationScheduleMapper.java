package com.parttime.cservice.mapper;

import com.parttime.cservice.pojo.entity.ApplicationSchedule;
import java.util.List;

public interface ApplicationScheduleMapper {
    void insert(ApplicationSchedule record);
    List<ApplicationSchedule> findByApplicationId(Long applicationId);
}
