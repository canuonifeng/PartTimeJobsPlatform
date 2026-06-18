package com.parttime.enterprise.mapper;

import com.parttime.enterprise.pojo.entity.CompanyWorker;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface CompanyWorkerMapper {
    int upsert(@Param("companyId") Long companyId, @Param("workerId") Long workerId);
    List<CompanyWorker> findByCompanyId(@Param("companyId") Long companyId, @Param("keyword") String keyword);
    List<CompanyWorker> findByCompanyIdPage(@Param("companyId") Long companyId, @Param("keyword") String keyword, @Param("offset") int offset, @Param("pageSize") int pageSize);
    long countByCompanyId(@Param("companyId") Long companyId, @Param("keyword") String keyword);
    Optional<CompanyWorker> findById(@Param("id") Long id);
    int updateStatus(@Param("id") Long id, @Param("status") String status);
}
