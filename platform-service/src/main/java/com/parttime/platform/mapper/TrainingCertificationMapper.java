package com.parttime.platform.mapper;

import com.parttime.platform.pojo.entity.TrainingCertification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface TrainingCertificationMapper {

    int insert(TrainingCertification certification);

    Optional<TrainingCertification> findById(@Param("id") Long id);

    Optional<TrainingCertification> findByCode(@Param("code") String code);

    List<TrainingCertification> findAll();

    int update(TrainingCertification certification);

    int updateStatus(@Param("id") Long id, @Param("status") String status);
}
