package com.parttime.enterprise.mapper;

import com.parttime.enterprise.pojo.entity.AttendanceCorrection;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface AttendanceCorrectionMapper {

    int insert(AttendanceCorrection correction);

    Optional<AttendanceCorrection> findById(Long id);

    List<AttendanceCorrection> findByShiftId(Long shiftId);

    List<AttendanceCorrection> findByWorkerId(Long workerId);

    List<AttendanceCorrection> findByStatus(String status);

    List<AttendanceCorrection> search(@Param("status") String status,
                                      @Param("keyword") String keyword,
                                      @Param("dateFrom") String dateFrom,
                                      @Param("dateTo") String dateTo,
                                      @Param("offset") Integer offset,
                                      @Param("limit") Integer limit);

    int countSearch(@Param("status") String status,
                    @Param("keyword") String keyword,
                    @Param("dateFrom") String dateFrom,
                    @Param("dateTo") String dateTo);

    int update(AttendanceCorrection correction);
}
