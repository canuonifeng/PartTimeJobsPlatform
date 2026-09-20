package com.parttime.platform.mapper;

import com.parttime.platform.pojo.entity.AttendanceRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface AttendanceRecordMapper {

    Optional<AttendanceRecord> findById(Long id);

    List<AttendanceRecord> findByJobId(Long jobId);

    List<AttendanceRecord> findByFilters(@Param("status") String status,
                                          @Param("companyId") Long companyId,
                                          @Param("keyword") String keyword);

    int updateStatus(@Param("id") Long id,
                     @Param("status") String status,
                     @Param("remark") String remark);
}
