package com.parttime.enterprise.service.impl;

import com.parttime.enterprise.mapper.JobTagMapper;
import com.parttime.enterprise.pojo.entity.JobTag;
import com.parttime.enterprise.pojo.entity.JobTagGroup;
import com.parttime.enterprise.pojo.vo.JobTagGroupVO;
import com.parttime.enterprise.pojo.vo.JobTagVO;
import com.parttime.enterprise.service.JobTagService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class JobTagServiceImpl implements JobTagService {

    @Resource
    private JobTagMapper jobTagMapper;

    @Override
    public List<JobTagGroupVO> getActiveGroups() {
        List<JobTagGroup> groups = jobTagMapper.findActiveGroups();
        List<JobTag> tags = jobTagMapper.findActiveTags();
        Map<Long, List<JobTag>> byGroup = tags.stream().collect(Collectors.groupingBy(JobTag::getGroupId));
        return groups.stream().map(group -> {
            JobTagGroupVO response = toGroupResponse(group);
            response.setTags(byGroup.getOrDefault(group.getId(), List.of()).stream()
                    .map(this::toTagResponse)
                    .collect(Collectors.toList()));
            return response;
        }).collect(Collectors.toList());
    }

    private JobTagGroupVO toGroupResponse(JobTagGroup group) {
        JobTagGroupVO response = new JobTagGroupVO();
        response.setId(group.getId());
        response.setName(group.getName());
        response.setCode(group.getCode());
        response.setSortOrder(group.getSortOrder());
        response.setStatus(group.getStatus());
        response.setCreatedAt(group.getCreatedAt());
        response.setUpdatedAt(group.getUpdatedAt());
        return response;
    }

    private JobTagVO toTagResponse(JobTag tag) {
        JobTagVO response = new JobTagVO();
        response.setId(tag.getId());
        response.setGroupId(tag.getGroupId());
        response.setGroupName(tag.getGroupName());
        response.setName(tag.getName());
        response.setCode(tag.getCode());
        response.setSortOrder(tag.getSortOrder());
        response.setStatus(tag.getStatus());
        response.setCreatedAt(tag.getCreatedAt());
        response.setUpdatedAt(tag.getUpdatedAt());
        return response;
    }
}
