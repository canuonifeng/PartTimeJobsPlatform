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

    List<AnnotationTaskOrder> findPage(@Param("companyId") Long companyId,
                                       @Param("workerId") Long workerId,
                                       @Param("jobId") Long jobId,
                                       @Param("status") String status,
                                       @Param("offset") int offset,
                                       @Param("pageSize") int pageSize);

    int countPage(@Param("companyId") Long companyId,
                  @Param("workerId") Long workerId,
                  @Param("jobId") Long jobId,
                  @Param("status") String status);
}
