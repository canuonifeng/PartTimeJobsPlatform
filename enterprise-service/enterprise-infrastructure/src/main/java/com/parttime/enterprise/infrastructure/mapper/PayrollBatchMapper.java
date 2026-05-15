package com.parttime.enterprise.infrastructure.mapper;

import com.parttime.enterprise.core.domain.PayrollBatch;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface PayrollBatchMapper {

    int insert(PayrollBatch batch);

    Optional<PayrollBatch> findById(Long id);

    List<PayrollBatch> findByCompanyId(Long companyId);

    List<PayrollBatch> findByCompanyIdAndStatus(@Param("companyId") Long companyId, @Param("status") String status);

    int updateStatus(@Param("id") Long id, @Param("status") String status);

    int update(PayrollBatch batch);
}
