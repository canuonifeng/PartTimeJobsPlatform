package com.parttime.enterprise.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface WorkerBatchMapper {

    @Select("SELECT id, name FROM c_worker WHERE id IN (${ids})")
    List<Map<String, Object>> findWorkerNamesByIds(@Param("ids") List<Long> ids);

    @Select("SELECT worker_id, birthday FROM worker_profiles WHERE worker_id IN (${ids})")
    List<Map<String, Object>> findWorkerBirthdaysByIds(@Param("ids") List<Long> ids);
}
