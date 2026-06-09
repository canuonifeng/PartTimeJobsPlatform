package com.parttime.cservice.mapper;

import com.parttime.cservice.pojo.entity.Enterprise;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EnterpriseMapper {
    Enterprise findById(Long id);
    List<Enterprise> findByIds(@Param("ids") List<Long> ids);
    int insert(Enterprise enterprise);
}
