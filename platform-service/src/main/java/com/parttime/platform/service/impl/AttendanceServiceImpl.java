package com.parttime.platform.service.impl;

import com.parttime.platform.exception.BusinessException;
import com.parttime.platform.mapper.AttendanceRecordMapper;
import com.parttime.platform.pojo.cmd.JobQueryCmd;
import com.parttime.platform.pojo.entity.AttendanceRecord;
import com.parttime.platform.pojo.vo.AttendanceRecordVO;
import com.parttime.platform.service.AttendanceService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    @Resource
    private AttendanceRecordMapper attendanceRecordMapper;

    @Override
    public List<AttendanceRecordVO> list(JobQueryCmd cmd) {
        List<AttendanceRecord> list = attendanceRecordMapper.findByFilters(cmd.getStatus(), cmd.getCompanyId(), cmd.getKeyword());
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public AttendanceRecordVO detail(Long id) {
        AttendanceRecord ar = attendanceRecordMapper.findById(id)
                .orElseThrow(() -> new BusinessException("考勤记录不存在: " + id));
        return toVO(ar);
    }

    @Override
    public void updateStatus(Long id, String status, String remark) {
        AttendanceRecord ar = attendanceRecordMapper.findById(id)
                .orElseThrow(() -> new BusinessException("考勤记录不存在: " + id));
        attendanceRecordMapper.updateStatus(id, status);
    }

    private AttendanceRecordVO toVO(AttendanceRecord ar) {
        AttendanceRecordVO vo = new AttendanceRecordVO();
        vo.setId(ar.getId());
        vo.setShiftId(ar.getShiftId());
        vo.setJobId(ar.getJobId());
        vo.setJobTitle(ar.getJobTitle());
        vo.setCompanyId(ar.getCompanyId());
        vo.setCompanyName(ar.getCompanyName());
        vo.setWorkerId(ar.getWorkerId());
        vo.setWorkerName(ar.getWorkerName());
        vo.setWorkerPhone(ar.getWorkerPhone());
        vo.setCheckInTime(ar.getCheckInTime());
        vo.setCheckOutTime(ar.getCheckOutTime());
        vo.setTotalHours(ar.getTotalHours());
        vo.setScheduledPay(ar.getScheduledPay());
        vo.setPayablePay(ar.getPayablePay());
        vo.setSettlementStatus(ar.getSettlementStatus());
        vo.setStatus(ar.getStatus());
        vo.setRemark(ar.getRemark());
        vo.setCreatedAt(ar.getCreatedAt());
        return vo;
    }
}
