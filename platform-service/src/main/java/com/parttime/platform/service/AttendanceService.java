package com.parttime.platform.service;

import com.parttime.platform.pojo.cmd.JobQueryCmd;
import com.parttime.platform.pojo.vo.AttendanceRecordVO;

import java.util.List;

public interface AttendanceService {
    List<AttendanceRecordVO> list(JobQueryCmd cmd);
    List<AttendanceRecordVO> listByJobId(Long jobId);
    AttendanceRecordVO detail(Long id);
    void updateStatus(Long id, String status, String remark);
}
