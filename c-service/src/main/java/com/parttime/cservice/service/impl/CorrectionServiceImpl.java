package com.parttime.cservice.service.impl;

import com.parttime.cservice.enums.CorrectionStatus;
import com.parttime.cservice.mapper.AttendanceCorrectionMapper;
import com.parttime.cservice.mapper.ShiftMapper;
import com.parttime.cservice.pojo.entity.AttendanceCorrectionEntity;
import com.parttime.cservice.pojo.entity.ShiftEntity;
import com.parttime.cservice.service.CorrectionService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
public class CorrectionServiceImpl implements CorrectionService {

    @Resource
    private AttendanceCorrectionMapper correctionMapper;
    @Resource
    private ShiftMapper shiftMapper;

    @Override
    public void submitCorrection(Long workerId, Long shiftId, String reason) {
        if (shiftId == null || reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("shiftId and reason are required");
        }

        ShiftEntity shift = shiftMapper.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("Shift not found: " + shiftId));

        if (!workerId.equals(shift.getWorkerId())) {
            throw new IllegalArgumentException("This shift does not belong to you");
        }

        LocalDateTime shiftEnd = LocalDateTime.of(shift.getShiftDate(), shift.getEndTime());
        if (LocalDateTime.now().isBefore(shiftEnd)) {
            throw new IllegalArgumentException("Shift has not ended yet");
        }

        Optional<AttendanceCorrectionEntity> existing = correctionMapper.findByShiftId(shiftId);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Correction already submitted for this shift");
        }

        AttendanceCorrectionEntity correction = new AttendanceCorrectionEntity();
        correction.setShiftId(shiftId);
        correction.setWorkerId(workerId);
        correction.setReason(reason.trim());
        correction.setStatus(CorrectionStatus.PENDING.name());
        correction.setCreatedAt(LocalDateTime.now());
        correctionMapper.insert(correction);
    }

    @Override
    public Map<String, Object> getCorrectionStatus(Long workerId, Long shiftId) {
        Optional<AttendanceCorrectionEntity> existing = correctionMapper.findByShiftId(shiftId);

        if (existing.isPresent()) {
            AttendanceCorrectionEntity c = existing.get();
            return Map.of(
                    "eligible", false,
                    "existingRequest", Map.of("id", c.getId(), "status", c.getStatus())
            );
        }

        ShiftEntity shift = shiftMapper.findById(shiftId).orElse(null);
        if (shift == null || !workerId.equals(shift.getWorkerId())) {
            return Map.of("eligible", false);
        }

        LocalDateTime shiftEnd = LocalDateTime.of(shift.getShiftDate(), shift.getEndTime());
        boolean ended = LocalDateTime.now().isAfter(shiftEnd);

        return Map.of("eligible", ended);
    }
}
