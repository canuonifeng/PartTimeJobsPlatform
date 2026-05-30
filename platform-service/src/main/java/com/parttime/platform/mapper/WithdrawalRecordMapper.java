package com.parttime.platform.mapper;

import com.parttime.platform.pojo.entity.WithdrawalRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface WithdrawalRecordMapper {
    List<WithdrawalRecord> findPage(@Param("workerId") Long workerId,
                                    @Param("status") String status,
                                    @Param("startTime") LocalDateTime startTime,
                                    @Param("endTime") LocalDateTime endTime,
                                    @Param("offset") int offset,
                                    @Param("pageSize") int pageSize);

    long countPage(@Param("workerId") Long workerId,
                   @Param("status") String status,
                   @Param("startTime") LocalDateTime startTime,
                   @Param("endTime") LocalDateTime endTime);
}
