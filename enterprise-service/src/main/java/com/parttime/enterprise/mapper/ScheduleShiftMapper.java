package com.parttime.enterprise.mapper;

import com.parttime.enterprise.pojo.entity.ScheduleShift;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Mapper
public interface ScheduleShiftMapper {

    int insert(ScheduleShift shift);

    Optional<ScheduleShift> findById(Long id);

    List<ScheduleShift> findAll();

    List<ScheduleShift> findByCompanyId(@Param("companyId") Long companyId);

    List<ScheduleShift> findByJobId(Long jobId);

    List<ScheduleShift> findByWorkerId(Long workerId);

    List<ScheduleShift> findByJobIdAndDate(@Param("jobId") Long jobId, @Param("shiftDate") LocalDate shiftDate);

    List<ScheduleShift> findPage(@Param("companyId") Long companyId,
                                 @Param("jobId") Long jobId,
                                 @Param("workerId") Long workerId,
                                 @Param("shiftDate") LocalDate shiftDate,
                                 @Param("offset") int offset,
                                 @Param("pageSize") int pageSize);

    List<ScheduleShift> findPageByStatus(@Param("companyId") Long companyId,
                                 @Param("jobId") Long jobId,
                                 @Param("workerId") Long workerId,
                                 @Param("shiftDate") LocalDate shiftDate,
                                 @Param("status") String status,
                                 @Param("offset") int offset,
                                 @Param("pageSize") int pageSize);

    int countPage(@Param("companyId") Long companyId,
                  @Param("jobId") Long jobId,
                  @Param("workerId") Long workerId,
                  @Param("shiftDate") LocalDate shiftDate);

    int countPageByStatus(@Param("companyId") Long companyId,
                  @Param("jobId") Long jobId,
                  @Param("workerId") Long workerId,
                  @Param("shiftDate") LocalDate shiftDate,
                  @Param("status") String status);

    List<ScheduleShift> findByWorkerIdAndDateRange(
            @Param("workerId") Long workerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    List<ScheduleShift> findByApplicationId(Long applicationId);

    List<ScheduleShift> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    int update(ScheduleShift shift);

    int updateStatus(@Param("id") Long id, @Param("status") String status);

    int delete(Long id);

    int cancelShift(Long id);

    List<ScheduleShift> findCompletedByWorkerIdAndCompanyId(@Param("workerId") Long workerId, @Param("companyId") Long companyId);

    List<ScheduleShift> findByIds(@Param("ids") List<Long> ids);
}
