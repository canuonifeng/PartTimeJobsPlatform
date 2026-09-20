package com.parttime.platform.service.impl;

import com.parttime.platform.exception.BusinessException;
import com.parttime.platform.mapper.AttendanceRecordMapper;
import com.parttime.platform.pojo.cmd.JobQueryCmd;
import com.parttime.platform.pojo.entity.AttendanceRecord;
import com.parttime.platform.pojo.vo.AttendanceRecordVO;
import com.parttime.platform.service.AttendanceService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    @Resource
    private AttendanceRecordMapper attendanceRecordMapper;

    @Override
    public List<AttendanceRecordVO> list(JobQueryCmd cmd) {
        List<AttendanceRecord> list = attendanceRecordMapper.findByFilters(
                cmd.getStatus(), cmd.getCompanyId(), cmd.getKeyword());
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public List<AttendanceRecordVO> listByJobId(Long jobId) {
        List<AttendanceRecord> list = attendanceRecordMapper.findByJobId(jobId);
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public AttendanceRecordVO detail(Long id) {
        AttendanceRecord entity = attendanceRecordMapper.findById(id)
                .orElseThrow(() -> new BusinessException("考勤记录不存在: " + id));
        return toVO(entity);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, String status, String remark) {
        AttendanceRecord entity = attendanceRecordMapper.findById(id)
                .orElseThrow(() -> new BusinessException("考勤记录不存在: " + id));
        attendanceRecordMapper.updateStatus(entity.getId(), status, remark);
    }

    private AttendanceRecordVO toVO(AttendanceRecord e) {
        AttendanceRecordVO vo = new AttendanceRecordVO();
        vo.setId(e.getId());
        vo.setShiftId(e.getShiftId());
        vo.setJobId(e.getJobId());
        vo.setJobTitle(e.getJobTitle());
        vo.setCompanyId(e.getCompanyId());
        vo.setCompanyName(e.getCompanyName());
        vo.setWorkerId(e.getWorkerId());
        vo.setWorkerName(e.getWorkerName());
        vo.setWorkerPhone(e.getWorkerPhone());
        vo.setCheckInTime(e.getCheckInTime());
        vo.setCheckOutTime(e.getCheckOutTime());
        vo.setTotalHours(e.getTotalHours());
        vo.setScheduledPay(e.getScheduledPay());
        vo.setPayablePay(e.getPayablePay());
        vo.setSettlementStatus(e.getSettlementStatus());
        vo.setStatus(e.getStatus());
        vo.setRemark(e.getRemark());
        vo.setCreatedAt(e.getCreatedAt());
        return vo;
    }
}
