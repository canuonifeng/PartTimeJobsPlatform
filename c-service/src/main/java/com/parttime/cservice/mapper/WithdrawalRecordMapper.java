package com.parttime.cservice.mapper;

import com.parttime.cservice.pojo.entity.WithdrawalRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface WithdrawalRecordMapper {
    int insert(WithdrawalRecord record);
    Optional<WithdrawalRecord> findById(Long id);
    List<WithdrawalRecord> findByWorkerId(Long workerId);
    int updateStatus(@Param("id") Long id, @Param("status") String status);
}
