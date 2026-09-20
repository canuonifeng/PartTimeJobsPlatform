package com.parttime.platform.service;

import com.parttime.platform.pojo.cmd.OperationLogQueryCmd;
import com.parttime.platform.pojo.vo.OperationLogVO;

import java.util.List;

public interface OperationLogService {

    List<OperationLogVO> list(OperationLogQueryCmd cmd);
}
