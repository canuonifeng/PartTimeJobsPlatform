package com.parttime.enterprise.core.service;

import com.parttime.enterprise.api.dto.*;
import com.parttime.enterprise.core.domain.AttendanceRecord;
import com.parttime.enterprise.core.domain.ScheduleShift;
import com.parttime.enterprise.core.domain.ScheduleTemplate;
import com.parttime.enterprise.core.domain.ScheduleTemplateSlot;
import com.parttime.enterprise.core.repository.AttendanceRecordRepository;
import com.parttime.enterprise.core.repository.ScheduleShiftRepository;
import com.parttime.enterprise.core.repository.ScheduleTemplateRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class ScheduleService {

    private final ScheduleTemplateRepository templateRepository;
    private final ScheduleShiftRepository shiftRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;

    public ScheduleService(ScheduleTemplateRepository templateRepository,
                           ScheduleShiftRepository shiftRepository,
                           AttendanceRecordRepository attendanceRecordRepository) {
        this.templateRepository = templateRepository;
        this.shiftRepository = shiftRepository;
        this.attendanceRecordRepository = attendanceRecordRepository;
    }

    public ScheduleTemplateResponse createTemplate(ScheduleTemplateRequest request) {
        ScheduleTemplate template = new ScheduleTemplate();
        template.setCompanyId(request.getCompanyId());
        template.setName(request.getName());
        template.setDescription(request.getDescription());
        templateRepository.save(template);

        if (request.getSlots() != null) {
            for (ScheduleTemplateSlotRequest slotReq : request.getSlots()) {
                ScheduleTemplateSlot slot = new ScheduleTemplateSlot();
                slot.setTemplateId(template.getId());
                slot.setDayOfWeek(slotReq.getDayOfWeek());
                slot.setStartTime(slotReq.getStartTime());
                slot.setEndTime(slotReq.getEndTime());
                slot.setMaxWorkers(slotReq.getMaxWorkers());
                slot.setLocationLat(slotReq.getLocationLat());
                slot.setLocationLng(slotReq.getLocationLng());
                slot.setLocationRadius(slotReq.getLocationRadius());
                slot.setLocationName(slotReq.getLocationName());
                templateRepository.saveSlot(slot);
            }
        }

        return toTemplateResponse(template);
    }

    public ScheduleTemplateResponse getTemplateById(Long id) {
        ScheduleTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ScheduleTemplate not found: " + id));
        return toFullTemplateResponse(template);
    }

    public List<ScheduleTemplateResponse> getTemplatesByCompany(Long companyId) {
        return templateRepository.findByCompanyId(companyId).stream()
                .map(this::toTemplateResponse)
                .collect(Collectors.toList());
    }

    public ScheduleTemplateResponse updateTemplate(Long id, ScheduleTemplateRequest request) {
        ScheduleTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ScheduleTemplate not found: " + id));
        template.setName(request.getName());
        template.setDescription(request.getDescription());
        templateRepository.update(template);

        templateRepository.deleteSlotsByTemplateId(id);
        if (request.getSlots() != null) {
            for (ScheduleTemplateSlotRequest slotReq : request.getSlots()) {
                ScheduleTemplateSlot slot = new ScheduleTemplateSlot();
                slot.setTemplateId(id);
                slot.setDayOfWeek(slotReq.getDayOfWeek());
                slot.setStartTime(slotReq.getStartTime());
                slot.setEndTime(slotReq.getEndTime());
                slot.setMaxWorkers(slotReq.getMaxWorkers());
                slot.setLocationLat(slotReq.getLocationLat());
                slot.setLocationLng(slotReq.getLocationLng());
                slot.setLocationRadius(slotReq.getLocationRadius());
                slot.setLocationName(slotReq.getLocationName());
                templateRepository.saveSlot(slot);
            }
        }

        return toFullTemplateResponse(template);
    }

    public void deleteTemplate(Long id) {
        templateRepository.delete(id);
    }

    public ScheduleShiftResponse assignShift(ScheduleShiftRequest request) {
        ScheduleShift shift = new ScheduleShift();
        shift.setJobId(request.getJobId());
        shift.setTemplateSlotId(request.getTemplateSlotId());
        shift.setWorkerId(request.getWorkerId());
        shift.setShiftDate(request.getShiftDate());
        shift.setStartTime(request.getStartTime());
        shift.setEndTime(request.getEndTime());
        shift.setLocationLat(request.getLocationLat());
        shift.setLocationLng(request.getLocationLng());
        shift.setLocationRadius(request.getLocationRadius());
        shift.setLocationName(request.getLocationName());
        shift.setStatus("SCHEDULED");
        shiftRepository.save(shift);
        return toShiftResponse(shift);
    }

    public List<ScheduleShiftResponse> getShifts(Long jobId, Long workerId, LocalDate shiftDate) {
        List<ScheduleShift> shifts;
        if (jobId != null && shiftDate != null) {
            shifts = shiftRepository.findByJobIdAndDate(jobId, shiftDate);
        } else if (jobId != null) {
            shifts = shiftRepository.findByJobId(jobId);
        } else if (workerId != null) {
            shifts = shiftRepository.findByWorkerId(workerId);
        } else {
            shifts = Collections.emptyList();
        }
        return shifts.stream().map(this::toShiftResponse).collect(Collectors.toList());
    }

    public void removeShift(Long id) {
        shiftRepository.delete(id);
    }

    public List<AttendanceReportResponse> getAttendanceReport(Long jobId, Long shiftId, LocalDate date) {
        List<ScheduleShift> shifts;
        if (shiftId != null) {
            ScheduleShift shift = shiftRepository.findById(shiftId)
                    .orElseThrow(() -> new RuntimeException("ScheduleShift not found: " + shiftId));
            shifts = List.of(shift);
        } else if (jobId != null && date != null) {
            shifts = shiftRepository.findByJobIdAndDate(jobId, date);
        } else if (jobId != null) {
            shifts = shiftRepository.findByJobId(jobId);
        } else {
            throw new RuntimeException("Either jobId or shiftId is required");
        }

        List<Long> shiftIds = shifts.stream().map(ScheduleShift::getId).collect(Collectors.toList());
        Map<Long, AttendanceRecord> recordMap = new HashMap<>();
        if (!shiftIds.isEmpty()) {
            List<AttendanceRecord> records = attendanceRecordRepository.findByShiftIds(shiftIds);
            for (AttendanceRecord record : records) {
                recordMap.put(record.getShiftId(), record);
            }
        }

        return shifts.stream().map(shift -> {
            AttendanceReportResponse report = new AttendanceReportResponse();
            report.setShiftId(shift.getId());
            report.setJobId(shift.getJobId());
            report.setWorkerId(shift.getWorkerId());
            report.setShiftDate(shift.getShiftDate());
            report.setStartTime(shift.getStartTime());
            report.setEndTime(shift.getEndTime());
            report.setShiftStatus(shift.getStatus());

            AttendanceRecord record = recordMap.get(shift.getId());
            if (record != null) {
                report.setCheckInTime(record.getCheckInTime());
                report.setCheckOutTime(record.getCheckOutTime());
                report.setTotalHours(record.getTotalHours());
                report.setAttendanceStatus(record.getStatus());
            } else {
                report.setAttendanceStatus("NO_RECORD");
            }

            return report;
        }).collect(Collectors.toList());
    }

    private ScheduleTemplateResponse toTemplateResponse(ScheduleTemplate template) {
        ScheduleTemplateResponse response = new ScheduleTemplateResponse();
        response.setId(template.getId());
        response.setCompanyId(template.getCompanyId());
        response.setName(template.getName());
        response.setDescription(template.getDescription());
        response.setCreatedAt(template.getCreatedAt());
        response.setUpdatedAt(template.getUpdatedAt());
        return response;
    }

    private ScheduleTemplateResponse toFullTemplateResponse(ScheduleTemplate template) {
        ScheduleTemplateResponse response = toTemplateResponse(template);
        List<ScheduleTemplateSlotResponse> slotResponses = templateRepository.findSlotsByTemplateId(template.getId())
                .stream().map(this::toSlotResponse).collect(Collectors.toList());
        response.setSlots(slotResponses);
        return response;
    }

    private ScheduleTemplateSlotResponse toSlotResponse(ScheduleTemplateSlot slot) {
        ScheduleTemplateSlotResponse response = new ScheduleTemplateSlotResponse();
        response.setId(slot.getId());
        response.setTemplateId(slot.getTemplateId());
        response.setDayOfWeek(slot.getDayOfWeek());
        response.setStartTime(slot.getStartTime());
        response.setEndTime(slot.getEndTime());
        response.setMaxWorkers(slot.getMaxWorkers());
        response.setLocationLat(slot.getLocationLat());
        response.setLocationLng(slot.getLocationLng());
        response.setLocationRadius(slot.getLocationRadius());
        response.setLocationName(slot.getLocationName());
        response.setCreatedAt(slot.getCreatedAt());
        response.setUpdatedAt(slot.getUpdatedAt());
        return response;
    }

    private ScheduleShiftResponse toShiftResponse(ScheduleShift shift) {
        ScheduleShiftResponse response = new ScheduleShiftResponse();
        response.setId(shift.getId());
        response.setJobId(shift.getJobId());
        response.setTemplateSlotId(shift.getTemplateSlotId());
        response.setWorkerId(shift.getWorkerId());
        response.setShiftDate(shift.getShiftDate());
        response.setStartTime(shift.getStartTime());
        response.setEndTime(shift.getEndTime());
        response.setLocationLat(shift.getLocationLat());
        response.setLocationLng(shift.getLocationLng());
        response.setLocationRadius(shift.getLocationRadius());
        response.setLocationName(shift.getLocationName());
        response.setStatus(shift.getStatus());
        response.setCreatedAt(shift.getCreatedAt());
        response.setUpdatedAt(shift.getUpdatedAt());
        return response;
    }
}
