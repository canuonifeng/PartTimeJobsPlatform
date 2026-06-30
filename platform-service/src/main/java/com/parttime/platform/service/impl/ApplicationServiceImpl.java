package com.parttime.platform.service.impl;

import com.parttime.platform.exception.BusinessException;
import com.parttime.platform.mapper.ScheduleApplicationMapper;
import com.parttime.platform.pojo.cmd.JobQueryCmd;
import com.parttime.platform.pojo.entity.ScheduleApplication;
import com.parttime.platform.pojo.vo.ApplicationVO;
import com.parttime.platform.service.ApplicationService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApplicationServiceImpl implements ApplicationService {

    @Resource
    private ScheduleApplicationMapper applicationMapper;

    @Override
    public List<ApplicationVO> list(JobQueryCmd cmd) {
        List<ScheduleApplication> list = applicationMapper.findByFilters(cmd.getStatus(), cmd.getCompanyId(), cmd.getKeyword());
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public ApplicationVO detail(Long id) {
        ScheduleApplication app = applicationMapper.findById(id)
                .orElseThrow(() -> new BusinessException("申请不存在: " + id));
        return toVO(app);
    }

    @Override
    public void accept(Long id) {
        ScheduleApplication app = applicationMapper.findById(id)
                .orElseThrow(() -> new BusinessException("申请不存在: " + id));
        applicationMapper.updateStatus(id, "ACCEPTED");
    }

    @Override
    public void reject(Long id) {
        ScheduleApplication app = applicationMapper.findById(id)
                .orElseThrow(() -> new BusinessException("申请不存在: " + id));
        applicationMapper.updateStatus(id, "REJECTED");
    }

    private ApplicationVO toVO(ScheduleApplication app) {
        ApplicationVO vo = new ApplicationVO();
        vo.setId(app.getId());
        vo.setJobId(app.getJobId());
        vo.setJobTitle(app.getJobTitle());
        vo.setCompanyId(app.getCompanyId());
        vo.setCompanyName(app.getCompanyName());
        vo.setWorkerId(app.getWorkerId());
        vo.setWorkerName(app.getWorkerName());
        vo.setWorkerPhone(app.getWorkerPhone());
        vo.setStatus(app.getStatus());
        vo.setAppliedAt(app.getAppliedAt());
        vo.setReviewedAt(app.getReviewedAt());
        return vo;
    }
}
