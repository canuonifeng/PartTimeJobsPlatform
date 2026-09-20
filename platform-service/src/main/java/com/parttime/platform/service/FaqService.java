package com.parttime.platform.service;

import com.parttime.platform.pojo.cmd.FaqQueryCmd;
import com.parttime.platform.pojo.cmd.FaqSaveCmd;
import com.parttime.platform.pojo.cmd.FaqSortCmd;
import com.parttime.platform.pojo.vo.FaqVO;

import java.util.List;

public interface FaqService {

    List<FaqVO> list(FaqQueryCmd cmd);

    FaqVO detail(Long id);

    FaqVO create(FaqSaveCmd cmd);

    FaqVO update(FaqSaveCmd cmd);

    void sort(FaqSortCmd cmd);

    void delete(Long id);
}
