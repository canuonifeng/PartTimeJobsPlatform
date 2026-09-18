package com.parttime.cservice.mapper;

import com.parttime.cservice.pojo.entity.AnnotationTaskOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AnnotationTaskOrderMapper {
    List<AnnotationTaskOrder> selectByWorkerId(@Param("workerId") Long workerId);
    AnnotationTaskOrder selectById(@Param("id") Long id);
}
