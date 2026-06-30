package com.parttime.platform.service;

import com.parttime.platform.pojo.vo.WithdrawalRecordVO;

import java.util.List;

public interface WithdrawalRecordService {
    List<WithdrawalRecordVO> list(String status, String keyword);
    WithdrawalRecordVO detail(Long id);
    void approve(Long id);
    void reject(Long id, String reason);
}
