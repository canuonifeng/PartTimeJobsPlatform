package com.parttime.platform.mapper;

import com.parttime.platform.pojo.entity.TrainingCourse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface TrainingCourseMapper {

    int insert(TrainingCourse course);

    Optional<TrainingCourse> findById(@Param("id") Long id);

    List<TrainingCourse> findAll();

    int update(TrainingCourse course);

    int updateStatus(@Param("id") Long id, @Param("status") String status);

    int deleteById(@Param("id") Long id);
}
