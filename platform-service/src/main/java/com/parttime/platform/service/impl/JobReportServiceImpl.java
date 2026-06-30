package com.parttime.platform.service.impl;

import com.parttime.platform.pojo.vo.JobReportVO;
import com.parttime.platform.service.JobReportService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class JobReportServiceImpl implements JobReportService {

    @Override
    public List<JobReportVO> list(String status) {
        List<JobReportVO> list = new ArrayList<>();
        String[] statuses = {"PENDING", "DISMISSED", "BANNED"};
        String[] reasons = {"虚假招聘", "薪资不符", "位置虚假", "信息不全", "其他"};
        for (int i = 1; i <= 20; i++) {
            JobReportVO vo = new JobReportVO();
            vo.setId((long) i);
            vo.setJobId((long) (i % 10 + 1));
            vo.setJobTitle("测试职位" + (i % 10 + 1));
            vo.setReporterId((long) (i % 100 + 1));
            vo.setReporterName("举报人" + (i % 100 + 1));
            vo.setReason(reasons[i % 5]);
            vo.setDescription("这是一条测试举报内容，编号" + i);
            vo.setStatus(statuses[i % 3]);
            vo.setReviewerId(i % 3 == 0 ? 1L : null);
            vo.setReviewRemark(i % 3 == 0 ? "已处理" : null);
            vo.setReviewedAt(i % 3 == 0 ? LocalDateTime.now().minusHours(i) : null);
            vo.setCreatedAt(LocalDateTime.now().minusHours(i));
            list.add(vo);
        }
        return list;
    }

    @Override
    public JobReportVO detail(Long id) {
        JobReportVO vo = new JobReportVO();
        vo.setId(id);
        vo.setJobId(1L);
        vo.setJobTitle("测试职位详情");
        vo.setReporterId(100L);
        vo.setReporterName("测试举报人");
        vo.setReason("虚假招聘");
        vo.setDescription("该职位信息与实际情况不符，存在虚假宣传");
        vo.setStatus("PENDING");
        vo.setCreatedAt(LocalDateTime.now().minusHours(2));
        return vo;
    }

    @Override
    public void dismiss(Long id) {
    }

    @Override
    public void ban(Long id) {
    }
}
