package com.parttime.platform.service.impl;

import com.parttime.platform.exception.BusinessException;
import com.parttime.platform.mapper.ScheduleApplicationMapper;
import com.parttime.platform.pojo.cmd.JobQueryCmd;
import com.parttime.platform.pojo.entity.ScheduleApplication;
import com.parttime.platform.pojo.vo.ApplicationVO;
import com.parttime.platform.service.ApplicationService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApplicationServiceImpl implements ApplicationService {

    @Resource
    private ScheduleApplicationMapper scheduleApplicationMapper;

    @Override
    public List<ApplicationVO> list(JobQueryCmd cmd) {
        List<ScheduleApplication> list = scheduleApplicationMapper.findByFilters(
                cmd.getStatus(), cmd.getCompanyId(), null, cmd.getKeyword());
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public List<ApplicationVO> listByJobId(Long jobId) {
        List<ScheduleApplication> list = scheduleApplicationMapper.findByJobId(jobId);
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public ApplicationVO detail(Long id) {
        ScheduleApplication entity = scheduleApplicationMapper.findById(id)
                .orElseThrow(() -> new BusinessException("报名记录不存在: " + id));
        return toVO(entity);
    }

    @Override
    @Transactional
    public void accept(Long id) {
        ScheduleApplication entity = scheduleApplicationMapper.findById(id)
                .orElseThrow(() -> new BusinessException("报名记录不存在: " + id));
        scheduleApplicationMapper.updateStatus(entity.getId(), "ACCEPTED");
    }

    @Override
    @Transactional
    public void reject(Long id) {
        ScheduleApplication entity = scheduleApplicationMapper.findById(id)
                .orElseThrow(() -> new BusinessException("报名记录不存在: " + id));
        scheduleApplicationMapper.updateStatus(entity.getId(), "REJECTED");
    }

    @Override
    @Transactional
    public void batchAccept(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        for (Long id : ids) {
            scheduleApplicationMapper.updateStatus(id, "ACCEPTED");
        }
    }

    @Override
    @Transactional
    public void batchReject(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        for (Long id : ids) {
            scheduleApplicationMapper.updateStatus(id, "REJECTED");
        }
    }

    private ApplicationVO toVO(ScheduleApplication e) {
        ApplicationVO vo = new ApplicationVO();
        vo.setId(e.getId());
        vo.setJobId(e.getJobId());
        vo.setJobTitle(e.getJobTitle());
        vo.setCompanyId(e.getCompanyId());
        vo.setCompanyName(e.getCompanyName());
        vo.setWorkerId(e.getWorkerId());
        vo.setWorkerName(e.getWorkerName());
        vo.setWorkerPhone(e.getWorkerPhone());
        vo.setStatus(e.getStatus());
        vo.setAppliedAt(e.getAppliedAt());
        vo.setReviewedAt(e.getReviewedAt());
        return vo;
    }
}
