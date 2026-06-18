package com.parttime.enterprise.mapper;

import com.parttime.enterprise.pojo.entity.ScheduleApplication;
import com.parttime.enterprise.pojo.vo.ScheduleApplicationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ScheduleApplicationMapper {

    int insert(ScheduleApplication application);

    Optional<ScheduleApplication> findById(Long id);

    List<ScheduleApplication> findAll();

    List<ScheduleApplication> findByJobId(Long jobId);

    List<ScheduleApplication> findByJobIdPaged(@Param("jobId") Long jobId, @Param("offset") int offset, @Param("limit") int limit);

    List<ScheduleApplication> findByCompanyId(@Param("companyId") Long companyId);

    List<ScheduleApplication> findByWorkerId(Long workerId);

    int countByJobId(Long jobId);

    int countByScheduleId(@Param("scheduleId") Long scheduleId);

    int countByJobIdAndStatus(@Param("jobId") Long jobId, @Param("status") String status);

    int countByScheduleIdAndStatus(@Param("scheduleId") Long scheduleId, @Param("status") String status);

    int updateStatus(@Param("id") Long id, @Param("status") String status);

    // VO methods with JOINs
    List<ScheduleApplicationVO> findVOByCompanyId(@Param("companyId") Long companyId);

    List<ScheduleApplicationVO> findVOByCompanyIdAndStatus(@Param("companyId") Long companyId, @Param("status") String status);

    List<ScheduleApplicationVO> findVOByJobId(@Param("jobId") Long jobId);

    List<ScheduleApplicationVO> findVOByJobIdAndStatus(@Param("jobId") Long jobId, @Param("status") String status);

    List<ScheduleApplicationVO> findVOPage(@Param("companyId") Long companyId,
                                           @Param("jobId") Long jobId,
                                           @Param("jobTitle") String jobTitle,
                                           @Param("status") String status,
                                           @Param("offset") int offset,
                                           @Param("pageSize") int pageSize);

    long countVO(@Param("companyId") Long companyId,
                 @Param("jobId") Long jobId,
                 @Param("jobTitle") String jobTitle,
                 @Param("status") String status);
}
