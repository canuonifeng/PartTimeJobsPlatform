package com.parttime.enterprise.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Mapper
public interface JobSyncMapper {

    void upsert(Long jobId, Long companyId, String companyName, String companyLogo,
                String title, String description, String location,
                String province, String city, String district, String address,
                BigDecimal latitude, BigDecimal longitude,
                Long categoryId,
                String rateType, BigDecimal rateAmount,
                String status, String scheduleInfo,
                Integer headcount, LocalDateTime deadline);
}
