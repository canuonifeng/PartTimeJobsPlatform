package com.parttime.platform.mapper;

import com.parttime.platform.pojo.entity.PushTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface PushTaskMapper {

    List<PushTask> findAll();

    Optional<PushTask> findById(@Param("id") Long id);

    int insert(PushTask task);

    int update(PushTask task);

    int updateStatus(@Param("id") Long id, @Param("status") String status);
}
