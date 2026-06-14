package com.parttime.cservice.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CompanyWorkerInsertMapper {
    int upsert(@Param("companyId") Long companyId, @Param("workerId") Long workerId);
}
