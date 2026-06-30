package com.parttime.platform.service.impl;

import com.parttime.platform.pojo.cmd.JobQueryCmd;
import com.parttime.platform.pojo.vo.AttendanceRecordVO;
import com.parttime.platform.service.AttendanceService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    @Override
    public List<AttendanceRecordVO> list(JobQueryCmd cmd) {
        List<AttendanceRecordVO> list = new ArrayList<>();
        String[] statuses = {"NORMAL", "LATE", "EARLY_LEAVE", "ABSENT"};
        for (int i = 1; i <= 20; i++) {
            AttendanceRecordVO vo = new AttendanceRecordVO();
            vo.setId((long) i);
            vo.setShiftId((long) (i % 10 + 1));
            vo.setJobId((long) (i % 10 + 1));
            vo.setJobTitle("职位" + (i % 10 + 1));
            vo.setCompanyId((long) (i % 5 + 1));
            vo.setCompanyName("企业" + (i % 5 + 1));
            vo.setWorkerId((long) (i % 50 + 1));
            vo.setWorkerName("工人" + (i % 50 + 1));
            vo.setCheckInTime(LocalDateTime.now().minusDays(i).withHour(8).withMinute(30));
            vo.setCheckOutTime(LocalDateTime.now().minusDays(i).withHour(17).withMinute(30));
            vo.setTotalHours(new BigDecimal(i % 2 == 0 ? 8 : 7.5));
            vo.setStatus(statuses[i % 4]);
            vo.setSettlementStatus(i % 2 == 0 ? "SETTLED" : "UNSETTLED");
            list.add(vo);
        }
        return list;
    }

    @Override
    public AttendanceRecordVO detail(Long id) {
        AttendanceRecordVO vo = new AttendanceRecordVO();
        vo.setId(id);
        vo.setShiftId(1L);
        vo.setJobId(1L);
        vo.setJobTitle("测试职位");
        vo.setCompanyId(1L);
        vo.setCompanyName("测试企业");
        vo.setWorkerId(1L);
        vo.setWorkerName("测试工人");
        vo.setCheckInTime(LocalDateTime.now().minusDays(1).withHour(8).withMinute(30));
        vo.setCheckOutTime(LocalDateTime.now().minusDays(1).withHour(17).withMinute(30));
        vo.setTotalHours(new BigDecimal("8.0"));
        vo.setStatus("NORMAL");
        vo.setSettlementStatus("UNSETTLED");
        vo.setRemark("北京市朝阳区");
        return vo;
    }

    @Override
    public void updateStatus(Long id, String status, String remark) {
    }
}
