package com.parttime.cservice.mapper;

import com.parttime.cservice.pojo.entity.WorkerLessonRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface WorkerLessonRecordMapper {

    Optional<WorkerLessonRecord> findByWorkerAndLesson(@Param("workerId") Long workerId, @Param("lessonId") Long lessonId);

    List<WorkerLessonRecord> findByWorkerId(@Param("workerId") Long workerId);

    int insert(WorkerLessonRecord record);

    int update(WorkerLessonRecord record);
}
