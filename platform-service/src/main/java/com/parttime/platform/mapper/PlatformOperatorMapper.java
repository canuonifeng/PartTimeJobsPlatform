package com.parttime.platform.mapper;

import com.parttime.platform.pojo.entity.PlatformOperator;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface PlatformOperatorMapper {

    List<PlatformOperator> findAll();

    Optional<PlatformOperator> findById(@Param("id") Long id);

    Optional<PlatformOperator> findByUsername(@Param("username") String username);

    int insert(PlatformOperator operator);

    int update(PlatformOperator operator);

    int updateStatus(@Param("id") Long id, @Param("status") String status);

    int updatePassword(@Param("id") Long id, @Param("password") String password);
}
