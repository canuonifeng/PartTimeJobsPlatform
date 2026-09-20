package com.parttime.platform.service;

import com.parttime.platform.pojo.cmd.ComplaintArbitrateCmd;
import com.parttime.platform.pojo.cmd.ComplaintHandleCmd;
import com.parttime.platform.pojo.cmd.ComplaintQueryCmd;
import com.parttime.platform.pojo.vo.ComplaintVO;

import java.util.List;

public interface ComplaintService {
    List<ComplaintVO> list(ComplaintQueryCmd cmd);
    ComplaintVO detail(Long id);
    void handle(ComplaintHandleCmd cmd);
    void arbitrate(ComplaintArbitrateCmd cmd);
    void close(Long id);
}
