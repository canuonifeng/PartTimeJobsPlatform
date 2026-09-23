package com.parttime.enterprise.mapper;

import com.parttime.enterprise.pojo.entity.JobSchedule;
import com.parttime.enterprise.pojo.vo.ScheduleApplicantVO;
import com.parttime.enterprise.pojo.vo.ScheduleManagementVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface JobScheduleMapper {

    int insert(JobSchedule schedule);

    Optional<JobSchedule> findById(Long id);

    List<JobSchedule> findByJobId(Long jobId);

    List<JobSchedule> findActiveByJobId(Long jobId);

    List<JobSchedule> findBatchesByJobId(Long jobId);

    String findLatestBatchCodeByJobId(@Param("jobId") Long jobId);

    int update(JobSchedule schedule);

    int updateBatch(@Param("id") Long id,
                    @Param("batchCode") String batchCode,
                    @Param("totalItems") Integer totalItems,
                    @Param("externalBatchId") String externalBatchId);

    int updateStatus(@Param("id") Long id, @Param("status") String status);

    int cancelSchedule(Long id);

    int cancelByJobId(Long jobId);

    int delete(Long id);

    int deleteByJobId(Long jobId);

    List<ScheduleManagementVO> findManagementPage(@Param("companyId") Long companyId,
                                                  @Param("jobId") Long jobId,
                                                  @Param("keyword") String keyword,
                                                  @Param("status") String status,
                                                  @Param("startDate") java.time.LocalDate startDate,
                                                  @Param("endDate") java.time.LocalDate endDate,
                                                  @Param("offset") int offset,
                                                  @Param("pageSize") int pageSize);

    long countManagementPage(@Param("companyId") Long companyId,
                             @Param("jobId") Long jobId,
                             @Param("keyword") String keyword,
                             @Param("status") String status,
                             @Param("startDate") java.time.LocalDate startDate,
                             @Param("endDate") java.time.LocalDate endDate);

    List<ScheduleApplicantVO> findApplicants(@Param("scheduleId") Long scheduleId,
                                             @Param("status") String status,
                                             @Param("offset") int offset,
                                             @Param("pageSize") int pageSize);

    long countApplicants(@Param("scheduleId") Long scheduleId, @Param("status") String status);

    JobSchedule findByExternalBatchId(String externalBatchId);
}
