package com.parttime.enterprise.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface WorkerSyncMapper {

    @Select("SELECT name FROM c_worker WHERE id = #{id}")
    String findWorkerNameById(Long id);

    @Select("SELECT phone FROM c_worker WHERE id = #{id}")
    String findWorkerPhoneById(Long id);

    @Select("SELECT birthday FROM worker_profiles WHERE worker_id = #{id}")
    java.time.LocalDate findWorkerBirthdayById(Long id);
}
