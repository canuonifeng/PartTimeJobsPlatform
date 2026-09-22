package com.parttime.cservice.mapper;

import com.parttime.cservice.pojo.entity.QuestionBankQuestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface QuestionBankQuestionMapper {

    int countPublishedByBankAndType(@Param("bankId") Long bankId, @Param("questionType") String questionType);

    List<QuestionBankQuestion> randomPublishedByBankAndType(@Param("bankId") Long bankId,
                                                             @Param("questionType") String questionType,
                                                             @Param("count") int count);
}
