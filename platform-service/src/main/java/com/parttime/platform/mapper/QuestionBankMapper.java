package com.parttime.platform.mapper;

import com.parttime.platform.pojo.entity.QuestionBank;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface QuestionBankMapper {

    int insert(QuestionBank questionBank);

    Optional<QuestionBank> findById(@Param("id") Long id);

    Optional<QuestionBank> findByName(@Param("name") String name);

    List<QuestionBank> findAll();

    int update(QuestionBank questionBank);

    int updateStatus(@Param("id") Long id, @Param("status") String status);

    int countByBankId(@Param("bankId") Long bankId);
}
