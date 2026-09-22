package com.parttime.platform.mapper;

import com.parttime.platform.pojo.entity.TrainingLesson;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface TrainingLessonMapper {

    int insert(TrainingLesson lesson);

    Optional<TrainingLesson> findById(@Param("id") Long id);

    List<TrainingLesson> findByCourseId(@Param("courseId") Long courseId);

    int update(TrainingLesson lesson);

    int updateStatus(@Param("id") Long id, @Param("status") String status);

    int updateSortOrder(@Param("id") Long id, @Param("sortOrder") Integer sortOrder);

    int deleteById(@Param("id") Long id);
}
