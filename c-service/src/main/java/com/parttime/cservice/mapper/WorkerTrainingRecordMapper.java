package com.parttime.cservice.mapper;

import com.parttime.cservice.pojo.entity.WorkerTrainingRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface WorkerTrainingRecordMapper {

    Optional<WorkerTrainingRecord> findByWorkerAndCourse(@Param("workerId") Long workerId, @Param("courseId") Long courseId);

    List<WorkerTrainingRecord> findByWorkerId(@Param("workerId") Long workerId);

    int insert(WorkerTrainingRecord record);

    int update(WorkerTrainingRecord record);
}
