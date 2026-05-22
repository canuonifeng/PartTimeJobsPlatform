package com.parttime.enterprise.mapper;

import com.parttime.enterprise.pojo.entity.SettlementBill;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Mapper
public interface SettlementBillMapper {

    int insert(SettlementBill bill);

    Optional<SettlementBill> findById(Long id);

    List<SettlementBill> findByCompanyId(@Param("companyId") Long companyId,
                                         @Param("workerName") String workerName,
                                         @Param("dateFrom") LocalDate dateFrom,
                                         @Param("dateTo") LocalDate dateTo,
                                         @Param("offset") int offset,
                                         @Param("pageSize") int pageSize);

    long countByCompanyId(@Param("companyId") Long companyId,
                          @Param("workerName") String workerName,
                          @Param("dateFrom") LocalDate dateFrom,
                          @Param("dateTo") LocalDate dateTo);
}
