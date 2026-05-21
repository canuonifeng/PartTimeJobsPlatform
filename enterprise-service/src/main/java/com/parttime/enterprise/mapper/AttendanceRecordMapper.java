package com.parttime.enterprise.mapper;

import com.parttime.enterprise.pojo.entity.AttendanceRecord;
import com.parttime.enterprise.pojo.vo.AttendanceHoursVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Mapper
public interface AttendanceRecordMapper {

    int insert(AttendanceRecord record);

    Optional<AttendanceRecord> findById(Long id);

    Optional<AttendanceRecord> findByShiftId(Long shiftId);

    List<AttendanceRecord> findByShiftIds(@Param("shiftIds") List<Long> shiftIds);

    int update(AttendanceRecord record);

    List<AttendanceHoursVO> findHours(@Param("companyId") Long companyId,
                                      @Param("workerName") String workerName,
                                      @Param("dateFrom") LocalDate dateFrom,
                                      @Param("dateTo") LocalDate dateTo,
                                      @Param("isPaid") Boolean isPaid,
                                      @Param("offset") int offset,
                                      @Param("pageSize") int pageSize);

    long countHours(@Param("companyId") Long companyId,
                    @Param("workerName") String workerName,
                    @Param("dateFrom") LocalDate dateFrom,
                    @Param("dateTo") LocalDate dateTo,
                    @Param("isPaid") Boolean isPaid);

    void updatePaidStatus(@Param("ids") List<Long> ids, @Param("isPaid") boolean isPaid);

    void deleteByIds(@Param("ids") List<Long> ids);
}
