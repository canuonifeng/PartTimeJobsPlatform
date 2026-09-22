package com.parttime.cservice.mapper;

import com.parttime.cservice.pojo.entity.QuestionBank;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface QuestionBankMapper {

    Optional<QuestionBank> findById(@Param("id") Long id);
}
