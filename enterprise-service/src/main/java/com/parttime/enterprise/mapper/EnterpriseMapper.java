package com.parttime.enterprise.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface EnterpriseMapper {

    @Select("SELECT company_name FROM enterprises WHERE id = #{id}")
    String findCompanyNameById(@Param("id") Long id);

    @Select("SELECT company_logo FROM enterprises WHERE id = #{id}")
    String findCompanyLogoById(@Param("id") Long id);

    @Select("SELECT id FROM enterprises WHERE email_suffix = #{emailSuffix}")
    Long findIdByEmailSuffix(@Param("emailSuffix") String emailSuffix);

    @Update("UPDATE enterprises SET company_logo = #{logoUrl} WHERE id = #{id}")
    void updateLogo(@Param("id") Long id, @Param("logoUrl") String logoUrl);
}
