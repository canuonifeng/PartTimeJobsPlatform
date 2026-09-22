package com.parttime.cservice.mapper;

import com.parttime.cservice.pojo.entity.TrainingLesson;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Mapper
public interface TrainingLessonMapper {

    List<TrainingLesson> findByCourseId(@Param("courseId") Long courseId);

    List<TrainingLesson> findByCourseIds(@Param("courseIds") Collection<Long> courseIds);

    Optional<TrainingLesson> findById(@Param("id") Long id);
}
