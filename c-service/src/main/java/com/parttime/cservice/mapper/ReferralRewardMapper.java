package com.parttime.cservice.mapper;

import com.parttime.cservice.pojo.entity.ReferralReward;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReferralRewardMapper {

    ReferralReward findByReferralRecordId(@Param("referralRecordId") Long referralRecordId);

    List<ReferralReward> findByReferrerIdPage(@Param("referrerId") Long referrerId,
                                               @Param("offset") int offset,
                                               @Param("pageSize") int pageSize);

    long countByReferrerId(@Param("referrerId") Long referrerId);

    void insert(ReferralReward referralReward);

    void updateStatus(@Param("id") Long id,
                      @Param("status") String status,
                      @Param("auditRemark") String auditRemark);
}
