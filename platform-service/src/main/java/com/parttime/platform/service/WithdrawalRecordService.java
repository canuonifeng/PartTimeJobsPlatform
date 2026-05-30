package com.parttime.platform.service;

import com.parttime.platform.pojo.vo.PageVO;
import com.parttime.platform.pojo.vo.WithdrawalRecordVO;

import java.time.LocalDateTime;

public interface WithdrawalRecordService {
    PageVO<WithdrawalRecordVO> listRecords(Long workerId, String status, LocalDateTime startTime, LocalDateTime endTime, int page, int pageSize);
}
