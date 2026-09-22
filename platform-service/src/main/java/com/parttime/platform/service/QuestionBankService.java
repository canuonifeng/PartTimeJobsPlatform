package com.parttime.platform.service;

import com.parttime.platform.pojo.cmd.QuestionBankCreateCmd;
import com.parttime.platform.pojo.cmd.QuestionBankUpdateCmd;
import com.parttime.platform.pojo.vo.QuestionBankVO;

import java.util.List;

public interface QuestionBankService {

    List<QuestionBankVO> list();

    QuestionBankVO create(QuestionBankCreateCmd cmd);

    QuestionBankVO update(QuestionBankUpdateCmd cmd);

    void toggle(Long id);
}
