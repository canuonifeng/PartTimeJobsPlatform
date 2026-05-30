package com.parttime.enterprise.mapper;

import com.parttime.enterprise.pojo.entity.EnterpriseTopUpRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface EnterpriseTopUpRecordMapper {

    void insert(EnterpriseTopUpRecord record);

    void updateStatus(@Param("id") Long id,
                      @Param("status") String status,
                      @Param("thirdPartySerialNo") String thirdPartySerialNo,
                      @Param("thirdPartyPlatform") String thirdPartyPlatform,
                      @Param("completedAt") LocalDateTime completedAt);
}
