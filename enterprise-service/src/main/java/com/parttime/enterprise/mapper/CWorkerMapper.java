package com.parttime.enterprise.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CWorkerMapper {

    @Select("SELECT name FROM c_worker WHERE id = #{id}")
    String findWorkerNameById(Long id);
}
