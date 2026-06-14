package com.parttime.enterprise.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface EnterpriseMapper {

    String findCompanyNameById(@Param("id") Long id);

    String findCompanyLogoById(@Param("id") Long id);

    Long findIdByEmailSuffix(@Param("emailSuffix") String emailSuffix);

    void updateLogo(@Param("id") Long id, @Param("logoUrl") String logoUrl);
}
