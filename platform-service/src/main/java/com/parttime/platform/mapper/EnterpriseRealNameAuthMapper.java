package com.parttime.platform.mapper;

import com.parttime.platform.pojo.entity.EnterpriseRealNameAuth;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Mapper
public interface EnterpriseRealNameAuthMapper {
    List<EnterpriseRealNameAuth> findPage(@Param("status") String status,
                                          @Param("offset") int offset,
                                          @Param("pageSize") int pageSize);
    long countPage(@Param("status") String status);
    Optional<EnterpriseRealNameAuth> findById(@Param("id") Long id);
    int updateReview(@Param("id") Long id,
                     @Param("status") String status,
                     @Param("rejectReason") String rejectReason,
                     @Param("reviewerId") Long reviewerId,
                     @Param("reviewedAt") LocalDateTime reviewedAt);
}
