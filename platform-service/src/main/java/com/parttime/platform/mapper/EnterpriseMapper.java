package com.parttime.platform.mapper;

import com.parttime.platform.pojo.entity.Enterprise;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface EnterpriseMapper {

    List<Enterprise> findAll();

    List<Enterprise> findByStatus(@Param("status") String status);

    Optional<Enterprise> findById(@Param("id") Long id);

    List<Enterprise> findByIds(@Param("ids") List<Long> ids);

    int insert(Enterprise enterprise);

    int update(Enterprise enterprise);

    int updateStatus(@Param("id") Long id, @Param("status") String status);
}
