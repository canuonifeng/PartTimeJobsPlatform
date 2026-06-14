package com.parttime.enterprise.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface WorkerSyncMapper {

    @Select("SELECT name FROM c_worker WHERE id = #{id}")
    String findWorkerNameById(Long id);

    @Select("SELECT phone FROM c_worker WHERE id = #{id}")
    String findWorkerPhoneById(Long id);

    @Select("SELECT birthday FROM worker_profiles WHERE worker_id = #{id}")
    java.time.LocalDate findWorkerBirthdayById(Long id);

    @Select("SELECT gender FROM worker_profiles WHERE worker_id = #{id}")
    String findWorkerGenderById(Long id);

    @Select("SELECT COALESCE((SELECT status FROM worker_real_name_auth WHERE worker_id = #{id}), 'NONE')")
    String findWorkerRealNameStatusById(Long id);

    List<Map<String, Object>> findWorkerNamesByIds(@Param("ids") List<Long> ids);

    List<Map<String, Object>> findWorkerPhonesByIds(@Param("ids") List<Long> ids);

    List<Map<String, Object>> findWorkerGendersByIds(@Param("ids") List<Long> ids);

    List<Map<String, Object>> findWorkerBirthdaysByIds(@Param("ids") List<Long> ids);

    List<Map<String, Object>> findWorkerRealNameStatusesByIds(@Param("ids") List<Long> ids);
}
