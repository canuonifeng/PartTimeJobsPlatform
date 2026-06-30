package com.parttime.platform.service.impl;

import com.parttime.platform.exception.BusinessException;
import com.parttime.platform.mapper.JobMapper;
import com.parttime.platform.pojo.cmd.JobQueryCmd;
import com.parttime.platform.pojo.entity.Job;
import com.parttime.platform.pojo.vo.JobVO;
import com.parttime.platform.service.JobService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class JobServiceImpl implements JobService {

    @Resource
    private JobMapper jobMapper;

    @Override
    public List<JobVO> list(JobQueryCmd cmd) {
        List<Job> list = jobMapper.findByFilters(cmd.getStatus(), cmd.getCompanyId(), cmd.getCategoryId(), cmd.getKeyword());
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public JobVO detail(Long id) {
        Job job = jobMapper.findById(id)
                .orElseThrow(() -> new BusinessException("岗位不存在: " + id));
        return toVO(job);
    }

    @Override
    public void closeJob(Long id) {
        Job job = jobMapper.findById(id)
                .orElseThrow(() -> new BusinessException("岗位不存在: " + id));
        jobMapper.updateStatus(id, "CLOSED");
    }

    @Override
    public void reopenJob(Long id) {
        Job job = jobMapper.findById(id)
                .orElseThrow(() -> new BusinessException("岗位不存在: " + id));
        jobMapper.updateStatus(id, "PUBLISHED");
    }

    @Override
    public void setTop(Long id, Boolean isTop) {
        Job job = jobMapper.findById(id)
                .orElseThrow(() -> new BusinessException("岗位不存在: " + id));
        jobMapper.updateTop(id, isTop);
    }

    @Override
    public void setRecommended(Long id, Boolean isRecommended) {
        Job job = jobMapper.findById(id)
                .orElseThrow(() -> new BusinessException("岗位不存在: " + id));
        jobMapper.updateRecommended(id, isRecommended);
    }

    private JobVO toVO(Job j) {
        JobVO vo = new JobVO();
        vo.setId(j.getId());
        vo.setCompanyId(j.getCompanyId());
        vo.setCompanyName(j.getCompanyName());
        vo.setTitle(j.getTitle());
        vo.setDescription(j.getDescription());
        vo.setRequirements(j.getRequirements());
        vo.setContactName(j.getContactName());
        vo.setContactPhone(j.getContactPhone());
        vo.setLocation(j.getLocation());
        vo.setCategoryName(j.getCategoryName());
        vo.setHeadcount(j.getHeadcount());
        vo.setStatus(j.getStatus());
        vo.setIsTop(j.getIsTop());
        vo.setIsRecommended(j.getIsRecommended());
        vo.setDeadline(j.getDeadline());
        vo.setCreatedAt(j.getCreatedAt());
        vo.setUpdatedAt(j.getUpdatedAt());
        return vo;
    }
}
