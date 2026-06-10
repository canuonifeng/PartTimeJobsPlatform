package com.parttime.platform.mapper;

import com.parttime.platform.pojo.entity.Worker;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Optional;

@Mapper
public interface WorkerMapper {
    List<Worker> findAll(@Param("status") String status, @Param("keyword") String keyword);
    Optional<Worker> findById(@Param("id") Long id);
    List<Worker> findByIds(@Param("ids") List<Long> ids);
    int update(Worker worker);
    int updateStatus(@Param("id") Long id, @Param("status") String status);
}
