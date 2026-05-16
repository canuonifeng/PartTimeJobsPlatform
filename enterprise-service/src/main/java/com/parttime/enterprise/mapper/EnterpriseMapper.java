package com.parttime.enterprise.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EnterpriseMapper {

    @Select("SELECT company_name FROM enterprises WHERE id = #{id}")
    String findCompanyNameById(@Param("id") Long id);

    @Select("SELECT company_logo FROM enterprises WHERE id = #{id}")
    String findCompanyLogoById(@Param("id") Long id);
}
