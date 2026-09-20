package com.parttime.platform.mapper;

import com.parttime.platform.pojo.entity.Complaint;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ComplaintMapper {

    List<Complaint> findByFilters(@Param("status") String status,
                                   @Param("complaintType") String complaintType,
                                   @Param("complainantType") String complainantType,
                                   @Param("keyword") String keyword);

    Optional<Complaint> findById(@Param("id") Long id);

    int handle(@Param("id") Long id,
               @Param("handlerName") String handlerName,
               @Param("handleResult") String handleResult);

    int arbitrate(@Param("id") Long id,
                  @Param("handlerName") String handlerName,
                  @Param("conclusion") String conclusion);

    int close(@Param("id") Long id);

    List<Complaint> findActiveForMonitor();
}
