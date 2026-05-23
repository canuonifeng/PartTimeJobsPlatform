package com.parttime.cservice.mapper;

import com.parttime.cservice.pojo.entity.SettlementBillEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface SettlementBillMapper {
    List<SettlementBillEntity> findByWorkerId(@Param("workerId") Long workerId,
                                               @Param("offset") int offset,
                                               @Param("pageSize") int pageSize);
    long countByWorkerId(@Param("workerId") Long workerId);
}
