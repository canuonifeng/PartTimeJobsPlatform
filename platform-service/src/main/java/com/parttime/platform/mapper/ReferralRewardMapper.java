package com.parttime.platform.mapper;

import com.parttime.platform.pojo.entity.ReferralReward;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ReferralRewardMapper {
    ReferralReward findById(@Param("id") Long id);
    List<ReferralReward> findByStatusPage(@Param("status") String status,
                                          @Param("offset") int offset,
                                          @Param("pageSize") int pageSize);
    long countByStatus(@Param("status") String status);
    void insert(ReferralReward referralReward);
    void updateStatus(@Param("id") Long id,
                      @Param("status") String status,
                      @Param("auditRemark") String auditRemark);
    void updateGrantedAt(@Param("id") Long id,
                         @Param("grantedAt") LocalDateTime grantedAt);
}