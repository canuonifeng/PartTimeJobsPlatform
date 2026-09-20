package com.parttime.cservice.mapper;

import com.parttime.cservice.pojo.entity.TrainingCourse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface TrainingCourseMapper {

    List<TrainingCourse> findPublished();

    Optional<TrainingCourse> findById(@Param("id") Long id);

    Optional<TrainingCourse> findPublishedById(@Param("id") Long id);
}
