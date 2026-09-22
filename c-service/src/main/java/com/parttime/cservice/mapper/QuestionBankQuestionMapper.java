package com.parttime.cservice.mapper;

import com.parttime.cservice.pojo.entity.QuestionBankQuestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface QuestionBankQuestionMapper {

    Optional<QuestionBankQuestion> findById(@Param("id") Long id);

    int countPublishedByBankAndType(@Param("bankId") Long bankId, @Param("questionType") String questionType);

    List<QuestionBankQuestion> randomPublishedByBankAndType(@Param("bankId") Long bankId,
                                                             @Param("questionType") String questionType,
                                                             @Param("count") int count);
}
