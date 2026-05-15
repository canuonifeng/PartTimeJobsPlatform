package com.parttime.enterprise.infrastructure.mapper;

import com.parttime.enterprise.core.domain.WithdrawalRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface WithdrawalRecordMapper {

    int insert(WithdrawalRecord record);

    Optional<WithdrawalRecord> findById(Long id);

    List<WithdrawalRecord> findByWorkerId(Long workerId);

    List<WithdrawalRecord> findByWorkerIdAndStatus(@Param("workerId") Long workerId, @Param("status") String status);

    int updateStatus(@Param("id") Long id, @Param("status") String status);
}
