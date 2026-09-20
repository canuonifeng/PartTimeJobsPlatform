package com.parttime.platform.service.impl;

import com.parttime.platform.exception.BusinessException;
import com.parttime.platform.mapper.JobScheduleMapper;
import com.parttime.platform.pojo.cmd.JobQueryCmd;
import com.parttime.platform.pojo.entity.JobSchedule;
import com.parttime.platform.pojo.vo.JobScheduleVO;
import com.parttime.platform.service.JobScheduleService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class JobScheduleServiceImpl implements JobScheduleService {

    @Resource
    private JobScheduleMapper jobScheduleMapper;

    @Override
    public List<JobScheduleVO> list(JobQueryCmd cmd) {
        List<JobSchedule> list = jobScheduleMapper.findByFilters(
                cmd.getStatus(), cmd.getCompanyId(), cmd.getKeyword());
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public List<JobScheduleVO> listByJobId(Long jobId) {
        List<JobSchedule> list = jobScheduleMapper.findByJobId(jobId);
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public JobScheduleVO detail(Long id) {
        JobSchedule entity = jobScheduleMapper.findById(id)
                .orElseThrow(() -> new BusinessException("排班不存在: " + id));
        return toVO(entity);
    }

    @Override
    public void cancel(Long id) {
        JobSchedule entity = jobScheduleMapper.findById(id)
                .orElseThrow(() -> new BusinessException("排班不存在: " + id));
        jobScheduleMapper.updateStatus(entity.getId(), "CANCELLED");
    }

    private JobScheduleVO toVO(JobSchedule e) {
        JobScheduleVO vo = new JobScheduleVO();
        vo.setId(e.getId());
        vo.setJobId(e.getJobId());
        vo.setJobTitle(e.getJobTitle());
        vo.setCompanyId(e.getCompanyId());
        vo.setCompanyName(e.getCompanyName());
        vo.setScheduleDate(e.getScheduleDate());
        vo.setStartTime(e.getStartTime());
        vo.setEndTime(e.getEndTime());
        vo.setScheduleName(e.getScheduleName());
        vo.setSlotsAvailable(e.getSlotsAvailable());
        vo.setApplicationCount(e.getApplicationCount());
        vo.setHeadcount(e.getHeadcount());
        vo.setHourlyWage(e.getHourlyWage());
        vo.setStatus(e.getStatus());
        vo.setCreatedAt(e.getCreatedAt());
        return vo;
    }
}
