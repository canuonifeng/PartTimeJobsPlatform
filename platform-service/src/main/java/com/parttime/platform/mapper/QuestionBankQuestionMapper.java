package com.parttime.platform.mapper;

import com.parttime.platform.pojo.entity.QuestionBankQuestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface QuestionBankQuestionMapper {

    int insert(QuestionBankQuestion question);

    Optional<QuestionBankQuestion> findById(@Param("id") Long id);

    List<QuestionBankQuestion> findByBankId(@Param("bankId") Long bankId);

    List<QuestionBankQuestion> findPublishedByBankAndType(@Param("bankId") Long bankId,
                                                         @Param("questionType") String questionType);

    int update(QuestionBankQuestion question);

    int updateStatus(@Param("id") Long id, @Param("status") String status);

    int deleteById(@Param("id") Long id);
}
