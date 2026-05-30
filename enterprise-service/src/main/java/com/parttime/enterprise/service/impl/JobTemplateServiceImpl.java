package com.parttime.enterprise.service.impl;

import com.parttime.enterprise.exception.BusinessException;
import com.parttime.enterprise.mapper.JobTemplateMapper;
import com.parttime.enterprise.pojo.cmd.TemplateCreateCmd;
import com.parttime.enterprise.pojo.cmd.TemplateUpdateCmd;
import com.parttime.enterprise.pojo.entity.JobTemplate;
import com.parttime.enterprise.pojo.vo.JobTemplateVO;
import com.parttime.enterprise.service.JobTemplateService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JobTemplateServiceImpl implements JobTemplateService {

    @Resource
    private JobTemplateMapper jobTemplateMapper;

    @Override
    public List<JobTemplateVO> list(Long companyId) {
        return jobTemplateMapper.findByCompanyId(companyId)
                .stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public JobTemplateVO create(TemplateCreateCmd cmd, Long companyId) {
        JobTemplate entity = new JobTemplate();
        entity.setCompanyId(companyId);
        entity.setTitle(cmd.getTitle());
        entity.setDescription(cmd.getDescription());
        entity.setCategoryId(cmd.getCategoryId());
        entity.setImageUrl(cmd.getImageUrl());
        entity.setProvince(cmd.getProvince());
        entity.setCity(cmd.getCity());
        entity.setDistrict(cmd.getDistrict());
        entity.setAddress(cmd.getAddress());
        entity.setLatitude(cmd.getLatitude());
        entity.setLongitude(cmd.getLongitude());
        jobTemplateMapper.insert(entity);
        return toVO(entity);
    }

    @Override
    public JobTemplateVO update(TemplateUpdateCmd cmd) {
        JobTemplate entity = jobTemplateMapper.findById(cmd.getId())
                .orElseThrow(() -> new BusinessException("模版不存在"));
        entity.setTitle(cmd.getTitle());
        entity.setDescription(cmd.getDescription());
        entity.setCategoryId(cmd.getCategoryId());
        entity.setImageUrl(cmd.getImageUrl());
        entity.setProvince(cmd.getProvince());
        entity.setCity(cmd.getCity());
        entity.setDistrict(cmd.getDistrict());
        entity.setAddress(cmd.getAddress());
        entity.setLatitude(cmd.getLatitude());
        entity.setLongitude(cmd.getLongitude());
        jobTemplateMapper.update(entity);
        return toVO(entity);
    }

    @Override
    public void delete(Long id) {
        JobTemplate entity = jobTemplateMapper.findById(id)
                .orElseThrow(() -> new BusinessException("模版不存在"));
        jobTemplateMapper.deleteById(id);
    }

    private JobTemplateVO toVO(JobTemplate entity) {
        JobTemplateVO vo = new JobTemplateVO();
        vo.setId(entity.getId());
        vo.setTitle(entity.getTitle());
        vo.setDescription(entity.getDescription());
        vo.setCategoryId(entity.getCategoryId());
        vo.setImageUrl(entity.getImageUrl());
        vo.setProvince(entity.getProvince());
        vo.setCity(entity.getCity());
        vo.setDistrict(entity.getDistrict());
        vo.setAddress(entity.getAddress());
        vo.setLatitude(entity.getLatitude());
        vo.setLongitude(entity.getLongitude());
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }
}
