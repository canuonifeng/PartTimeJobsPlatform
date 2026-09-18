package com.parttime.enterprise.mapper;

import com.parttime.enterprise.pojo.entity.AnnotationTaskOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AnnotationTaskOrderMapper {

    int insert(AnnotationTaskOrder order);

    int update(AnnotationTaskOrder order);

    AnnotationTaskOrder selectById(Long id);

    List<AnnotationTaskOrder> selectByWorkerId(Long workerId);

    List<AnnotationTaskOrder> selectByJobId(Long jobId);

    AnnotationTaskOrder selectByApplicationId(Long applicationId);

    AnnotationTaskOrder selectByExternalSubmissionId(String externalSubmissionId);
}
