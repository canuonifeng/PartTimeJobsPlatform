package com.parttime.cservice.mapper;

import com.parttime.cservice.pojo.entity.ReferralRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReferralRecordMapper {

    ReferralRecord findByRefereeId(@Param("refereeId") Long refereeId);

    List<ReferralRecord> findByReferrerId(@Param("referrerId") Long referrerId);

    List<ReferralRecord> findByReferrerIdPage(@Param("referrerId") Long referrerId, @Param("offset") int offset, @Param("pageSize") int pageSize);

    void insert(ReferralRecord referralRecord);

    int countByReferrerId(@Param("referrerId") Long referrerId);
}
