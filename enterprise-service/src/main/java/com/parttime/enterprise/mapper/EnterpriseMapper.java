package com.parttime.enterprise.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EnterpriseMapper {

    @Select("SELECT company_name FROM enterprises WHERE id = #{id}")
    String findCompanyNameById(Long id);
}
