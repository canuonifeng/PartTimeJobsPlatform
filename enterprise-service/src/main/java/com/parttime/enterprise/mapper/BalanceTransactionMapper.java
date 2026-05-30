package com.parttime.enterprise.mapper;

import com.parttime.enterprise.pojo.entity.BalanceTransaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface BalanceTransactionMapper {
    int insert(BalanceTransaction transaction);

    Optional<BalanceTransaction> findByAttendanceRecordIdAndType(@Param("attendanceRecordId") Long attendanceRecordId,
                                                                 @Param("type") String type);
}
