package com.parttime.enterprise.infrastructure.mapper;

import com.parttime.enterprise.core.domain.PayrollItem;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface PayrollItemMapper {

    int insert(PayrollItem item);

    int insertBatch(List<PayrollItem> items);

    Optional<PayrollItem> findById(Long id);

    List<PayrollItem> findByBatchId(Long batchId);

    List<PayrollItem> findByBatchIdAndWorkerId(Long batchId, Long workerId);
}
