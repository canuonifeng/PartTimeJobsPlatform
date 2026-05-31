package com.parttime.platform.mapper;

import com.parttime.platform.pojo.entity.WorkerRealNameAuth;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Mapper
public interface WorkerRealNameAuthMapper {
    List<WorkerRealNameAuth> findPage(@Param("status") String status,
                                      @Param("offset") int offset,
                                      @Param("pageSize") int pageSize);
    long countPage(@Param("status") String status);
    Optional<WorkerRealNameAuth> findById(@Param("id") Long id);
    int updateReview(@Param("id") Long id,
                     @Param("status") String status,
                     @Param("rejectReason") String rejectReason,
                     @Param("reviewerId") Long reviewerId,
                     @Param("reviewedAt") LocalDateTime reviewedAt);
}
