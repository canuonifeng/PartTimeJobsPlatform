package com.parttime.enterprise.mapper;

import com.parttime.enterprise.pojo.entity.PayrollItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface PayrollItemMapper {

    int insert(PayrollItem item);

    int insertBatch(@Param("items") List<PayrollItem> items);

    Optional<PayrollItem> findById(Long id);

    List<PayrollItem> findByBatchId(Long batchId);

    List<PayrollItem> findByBatchIdAndWorkerId(@Param("batchId") Long batchId, @Param("workerId") Long workerId);
}
