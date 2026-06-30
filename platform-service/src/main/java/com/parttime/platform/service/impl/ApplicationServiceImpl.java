package com.parttime.platform.service.impl;

import com.parttime.platform.pojo.cmd.JobQueryCmd;
import com.parttime.platform.pojo.vo.ApplicationVO;
import com.parttime.platform.service.ApplicationService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ApplicationServiceImpl implements ApplicationService {

    @Override
    public List<ApplicationVO> list(JobQueryCmd cmd) {
        List<ApplicationVO> list = new ArrayList<>();
        String[] statuses = {"PENDING", "ACCEPTED", "REJECTED"};
        for (int i = 1; i <= 20; i++) {
            ApplicationVO vo = new ApplicationVO();
            vo.setId((long) i);
            vo.setJobId((long) (i % 10 + 1));
            vo.setJobTitle("测试职位" + (i % 10 + 1));
            vo.setCompanyId((long) (i % 5 + 1));
            vo.setCompanyName("企业" + (i % 5 + 1));
            vo.setWorkerId((long) (i % 50 + 1));
            vo.setWorkerName("工人" + (i % 50 + 1));
            vo.setWorkerPhone("138" + String.format("%08d", i));
            vo.setWage(new BigDecimal((i % 10 + 1) * 20));
            vo.setStatus(statuses[i % 3]);
            vo.setAppliedAt(LocalDateTime.now().minusHours(i));
            list.add(vo);
        }
        return list;
    }

    @Override
    public ApplicationVO detail(Long id) {
        ApplicationVO vo = new ApplicationVO();
        vo.setId(id);
        vo.setJobId(1L);
        vo.setJobTitle("测试职位详情");
        vo.setCompanyId(1L);
        vo.setCompanyName("测试企业");
        vo.setWorkerId(1L);
        vo.setWorkerName("测试工人");
        vo.setWorkerPhone("13800138000");
        vo.setWage(new BigDecimal("200.00"));
        vo.setStatus("PENDING");
        vo.setAppliedAt(LocalDateTime.now().minusHours(2));
        return vo;
    }

    @Override
    public void accept(Long id) {
    }

    @Override
    public void reject(Long id) {
    }
}
