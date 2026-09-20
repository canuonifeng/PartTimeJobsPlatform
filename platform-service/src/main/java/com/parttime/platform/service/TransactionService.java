package com.parttime.platform.service;

import com.parttime.platform.pojo.cmd.TransactionQueryCmd;
import com.parttime.platform.pojo.vo.TransactionOverviewVO;
import com.parttime.platform.pojo.vo.TransactionVO;

import java.util.List;

public interface TransactionService {
    List<TransactionVO> list(TransactionQueryCmd cmd);
    TransactionVO detail(Long id);
    TransactionOverviewVO overview();
}
