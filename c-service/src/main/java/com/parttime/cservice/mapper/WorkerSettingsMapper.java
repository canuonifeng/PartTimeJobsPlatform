package com.parttime.cservice.mapper;

import com.parttime.cservice.pojo.entity.WorkerSettings;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WorkerSettingsMapper {
    WorkerSettings findByWorkerId(@Param("workerId") Long workerId);

    int upsert(@Param("workerId") Long workerId,
               @Param("pushEnabled") Boolean pushEnabled,
               @Param("locationEnabled") Boolean locationEnabled,
               @Param("quietEnabled") Boolean quietEnabled);
}
