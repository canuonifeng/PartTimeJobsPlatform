package com.parttime.platform.mapper;

import com.parttime.platform.pojo.entity.ScheduleApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ScheduleApplicationMapper {

    Optional<ScheduleApplication> findById(Long id);

    List<ScheduleApplication> findByJobId(Long jobId);

    List<ScheduleApplication> findByFilters(@Param("status") String status,
                                             @Param("companyId") Long companyId,
                                             @Param("jobId") Long jobId,
                                             @Param("keyword") String keyword);

    int updateStatus(@Param("id") Long id, @Param("status") String status);
}
