package com.parttime.platform.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CJobMapper {

    int updateCompanyLogoByCompanyId(@Param("companyId") Long companyId,
                                     @Param("companyLogo") String companyLogo);
}
