package com.parttime.cservice.mapper;

import com.parttime.cservice.pojo.entity.ReferralCode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReferralCodeMapper {

    ReferralCode findByWorkerId(@Param("workerId") Long workerId);

    ReferralCode findByCode(@Param("code") String code);

    void insert(ReferralCode referralCode);
}
