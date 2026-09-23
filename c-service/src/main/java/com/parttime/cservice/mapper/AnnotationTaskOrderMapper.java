package com.parttime.cservice.mapper;

import com.parttime.cservice.pojo.entity.AnnotationTaskOrder;
import com.parttime.cservice.pojo.vo.AnnotationBatchProgress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AnnotationTaskOrderMapper {
    List<AnnotationTaskOrder> selectByWorkerId(@Param("workerId") Long workerId);
    AnnotationTaskOrder selectById(@Param("id") Long id);
    List<AnnotationBatchProgress> aggregateGrabbedByScheduleIds(@Param("scheduleIds") List<Long> scheduleIds);
}
