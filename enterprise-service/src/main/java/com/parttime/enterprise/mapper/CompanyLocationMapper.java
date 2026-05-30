package com.parttime.enterprise.mapper;

import com.parttime.enterprise.pojo.entity.CompanyLocation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface CompanyLocationMapper {
    List<CompanyLocation> findByCompanyId(@Param("companyId") Long companyId);
    Optional<CompanyLocation> findById(@Param("id") Long id);
    int insert(CompanyLocation location);
    int update(CompanyLocation location);
    int updateStatus(@Param("id") Long id, @Param("status") String status);
    int deleteById(@Param("id") Long id);
}
