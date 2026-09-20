package com.parttime.platform.mapper;

import com.parttime.platform.pojo.entity.JobSchedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface JobScheduleMapper {

    Optional<JobSchedule> findById(Long id);

    List<JobSchedule> findByJobId(Long jobId);

    List<JobSchedule> findByFilters(@Param("status") String status,
                                    @Param("companyId") Long companyId,
                                    @Param("keyword") String keyword);

    int updateStatus(@Param("id") Long id, @Param("status") String status);
}
