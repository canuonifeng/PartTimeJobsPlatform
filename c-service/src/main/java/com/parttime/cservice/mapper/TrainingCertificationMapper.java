package com.parttime.cservice.mapper;

import com.parttime.cservice.pojo.entity.TrainingCertification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface TrainingCertificationMapper {

    Optional<TrainingCertification> findById(@Param("id") Long id);

    List<TrainingCertification> findActiveByTaskType(@Param("taskType") String taskType);

    List<TrainingCertification> findByIds(@Param("ids") List<Long> ids);
}
