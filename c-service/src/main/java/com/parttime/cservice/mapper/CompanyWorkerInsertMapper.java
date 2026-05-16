package com.parttime.cservice.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CompanyWorkerInsertMapper {
    @Insert("INSERT INTO company_workers (company_id, worker_id, status) VALUES (#{companyId}, #{workerId}, 'ACTIVE') ON DUPLICATE KEY UPDATE last_contact_at = NOW(), updated_at = NOW()")
    int upsert(@Param("companyId") Long companyId, @Param("workerId") Long workerId);
}
