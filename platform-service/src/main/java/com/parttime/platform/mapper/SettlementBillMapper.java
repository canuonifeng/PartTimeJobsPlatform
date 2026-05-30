package com.parttime.platform.mapper;

import com.parttime.platform.pojo.entity.SettlementBill;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Mapper
public interface SettlementBillMapper {
    List<SettlementBill> findPage(@Param("companyId") Long companyId,
                                  @Param("workerName") String workerName,
                                  @Param("dateFrom") LocalDate dateFrom,
                                  @Param("dateTo") LocalDate dateTo,
                                  @Param("status") String status,
                                  @Param("offset") int offset,
                                  @Param("pageSize") int pageSize);

    long countPage(@Param("companyId") Long companyId,
                   @Param("workerName") String workerName,
                   @Param("dateFrom") LocalDate dateFrom,
                   @Param("dateTo") LocalDate dateTo,
                   @Param("status") String status);

    Optional<SettlementBill> findByShiftId(@Param("shiftId") Long shiftId);

    int updateBillStatus(@Param("id") Long id, @Param("status") String status);
}
