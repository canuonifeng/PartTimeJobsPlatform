package com.parttime.enterprise.service;

import com.parttime.enterprise.pojo.vo.CorrectionVO;
import java.util.Map;

public interface CorrectionService {

    Map<String, Object> listCorrections(String status, String keyword,
                                         String dateFrom, String dateTo,
                                         Integer page, Integer pageSize);

    void approve(Long id, Long processorId);

    void reject(Long id, Long processorId, String rejectReason);
}
