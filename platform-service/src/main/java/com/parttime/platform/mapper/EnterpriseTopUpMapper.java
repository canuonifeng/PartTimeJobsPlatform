package com.parttime.platform.mapper;

import com.parttime.platform.pojo.entity.EnterpriseTopUp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface EnterpriseTopUpMapper {
    List<EnterpriseTopUp> findByFilters(@Param("status") String status, @Param("keyword") String keyword);
    Optional<EnterpriseTopUp> findById(Long id);
    int updateStatus(@Param("id") Long id, @Param("status") String status, @Param("auditor") String auditor, @Param("remark") String remark);
}
