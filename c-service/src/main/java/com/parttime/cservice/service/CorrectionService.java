package com.parttime.cservice.service;

import com.parttime.cservice.pojo.entity.AttendanceCorrectionEntity;

import java.util.Map;
import java.util.Optional;

public interface CorrectionService {
    void submitCorrection(Long workerId, Long shiftId, String reason);
    Map<String, Object> getCorrectionStatus(Long workerId, Long shiftId);
}
