package com.parttime.platform.mapper;

import com.parttime.platform.pojo.entity.Lead;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LeadMapper {

    int insert(Lead lead);
}
