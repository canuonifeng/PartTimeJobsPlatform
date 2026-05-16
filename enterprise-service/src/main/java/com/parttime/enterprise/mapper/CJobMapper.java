package com.parttime.enterprise.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

@Mapper
public interface CJobMapper {

    void upsert(@Param("jobId") Long jobId,
                @Param("companyId") Long companyId,
                @Param("companyName") String companyName,
                @Param("title") String title,
                @Param("description") String description,
                @Param("location") String location,
                @Param("province") String province,
                @Param("city") String city,
                @Param("district") String district,
                @Param("address") String address,
                @Param("latitude") BigDecimal latitude,
                @Param("longitude") BigDecimal longitude,
                @Param("categoryId") Long categoryId,
                @Param("rateType") String rateType,
                @Param("rateAmount") BigDecimal rateAmount,
                @Param("status") String status,
                @Param("scheduleInfo") String scheduleInfo,
                @Param("headcount") Integer headcount,
                @Param("deadline") java.time.LocalDateTime deadline);
}
