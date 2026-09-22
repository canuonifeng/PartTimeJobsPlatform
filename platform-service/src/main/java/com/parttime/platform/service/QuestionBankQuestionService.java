package com.parttime.platform.service;

import com.parttime.platform.pojo.cmd.QuestionCreateCmd;
import com.parttime.platform.pojo.cmd.QuestionListCmd;
import com.parttime.platform.pojo.cmd.QuestionUpdateCmd;
import com.parttime.platform.pojo.vo.QuestionVO;

import java.util.List;

public interface QuestionBankQuestionService {

    List<QuestionVO> list(QuestionListCmd cmd);

    QuestionVO create(QuestionCreateCmd cmd);

    QuestionVO update(QuestionUpdateCmd cmd);

    void delete(Long id);

    void publish(Long id);

    void offline(Long id);
}
