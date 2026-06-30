package com.parttime.platform.service.impl;

import com.parttime.platform.exception.BusinessException;
import com.parttime.platform.mapper.JobScheduleMapper;
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
    public List<JobScheduleVO> listByJobId(Long jobId) {
        List<JobSchedule> list = jobScheduleMapper.findByJobId(jobId);
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public JobScheduleVO detail(Long id) {
        JobSchedule js = jobScheduleMapper.findById(id)
                .orElseThrow(() -> new BusinessException("排班不存在: " + id));
        return toVO(js);
    }

    @Override
    public void cancel(Long id) {
        JobSchedule js = jobScheduleMapper.findById(id)
                .orElseThrow(() -> new BusinessException("排班不存在: " + id));
        jobScheduleMapper.updateStatus(id, "CANCELLED");
    }

    private JobScheduleVO toVO(JobSchedule js) {
        JobScheduleVO vo = new JobScheduleVO();
        vo.setId(js.getId());
        vo.setJobId(js.getJobId());
        vo.setScheduleDate(js.getScheduleDate());
        vo.setStartTime(js.getStartTime());
        vo.setEndTime(js.getEndTime());
        vo.setScheduleName(js.getScheduleName());
        vo.setSlotsAvailable(js.getSlotsAvailable());
        vo.setApplicationCount(js.getApplicationCount());
        vo.setStatus(js.getStatus());
        vo.setCreatedAt(js.getCreatedAt());
        return vo;
    }
}
