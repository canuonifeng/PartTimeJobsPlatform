package com.parttime.platform.mapper;

import com.parttime.platform.pojo.entity.OperationActivity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface OperationActivityMapper {

    List<OperationActivity> findAll();

    Optional<OperationActivity> findById(@Param("id") Long id);

    int insert(OperationActivity activity);

    int update(OperationActivity activity);

    int updateStatus(@Param("id") Long id, @Param("status") String status);

    int deleteById(@Param("id") Long id);

    Long countApplicationsByDateRange(@Param("startTime") String startTime, @Param("endTime") String endTime);
}
