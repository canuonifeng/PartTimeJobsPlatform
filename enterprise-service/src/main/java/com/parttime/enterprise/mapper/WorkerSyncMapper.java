package com.parttime.enterprise.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface WorkerSyncMapper {

    String findWorkerNameById(Long id);

    String findWorkerPhoneById(Long id);

    java.time.LocalDate findWorkerBirthdayById(Long id);

    String findWorkerGenderById(Long id);

    String findWorkerRealNameStatusById(Long id);

    List<Map<String, Object>> findWorkerNamesByIds(@Param("ids") List<Long> ids);

    List<Map<String, Object>> findWorkerPhonesByIds(@Param("ids") List<Long> ids);

    List<Map<String, Object>> findWorkerGendersByIds(@Param("ids") List<Long> ids);

    List<Map<String, Object>> findWorkerBirthdaysByIds(@Param("ids") List<Long> ids);

    List<Map<String, Object>> findWorkerRealNameStatusesByIds(@Param("ids") List<Long> ids);
}
