package com.parttime.enterprise.mapper;

import com.parttime.enterprise.pojo.vo.ScheduleApplicationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ApplicationScheduleMapper {

    List<ScheduleApplicationVO> findByCompanyId(@Param("companyId") Long companyId);

    List<ScheduleApplicationVO> findByJobId(@Param("jobId") Long jobId);

    List<ScheduleApplicationVO> findByJobIdAndStatus(@Param("jobId") Long jobId, @Param("status") String status);
}
