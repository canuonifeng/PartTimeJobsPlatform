package com.parttime.platform.service.impl;

import com.parttime.platform.exception.BusinessException;
import com.parttime.platform.mapper.JobTagMapper;
import com.parttime.platform.pojo.cmd.JobTagCmd;
import com.parttime.platform.pojo.cmd.JobTagGroupCmd;
import com.parttime.platform.pojo.entity.JobTag;
import com.parttime.platform.pojo.entity.JobTagGroup;
import com.parttime.platform.pojo.vo.JobTagGroupVO;
import com.parttime.platform.pojo.vo.JobTagVO;
import com.parttime.platform.service.JobTagService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class JobTagServiceImpl implements JobTagService {

    private static final String ACTIVE = "ACTIVE";
    private static final String DISABLED = "DISABLED";

    @Resource
    private JobTagMapper jobTagMapper;

    @Override
    public List<JobTagGroupVO> getAllGroups() {
        List<JobTagGroup> groups = jobTagMapper.findAllGroups().stream()
                .sorted(groupComparator())
                .toList();
        Map<Long, List<JobTagVO>> tagsByGroup = jobTagMapper.findAllTags().stream()
                .sorted(tagComparator())
                .map(this::toTagVO)
                .collect(Collectors.groupingBy(JobTagVO::getGroupId));
        return groups.stream()
                .map(group -> {
                    JobTagGroupVO vo = toGroupVO(group);
                    vo.setTags(tagsByGroup.getOrDefault(group.getId(), List.of()));
                    return vo;
                })
                .toList();
    }

    @Override
    public JobTagGroupVO createGroup(JobTagGroupCmd cmd) {
        JobTagGroup group = new JobTagGroup();
        group.setName(cmd.getName());
        group.setCode(cmd.getCode());
        group.setSortOrder(cmd.getSortOrder());
        group.setStatus(normalizeStatus(cmd.getStatus()));
        jobTagMapper.insertGroup(group);
        return toGroupVO(group);
    }

    @Override
    public JobTagGroupVO updateGroup(Long id, JobTagGroupCmd cmd) {
        JobTagGroup group = jobTagMapper.findGroupById(id)
                .orElseThrow(() -> new BusinessException("JobTagGroup not found: " + id));
        group.setName(cmd.getName());
        group.setCode(cmd.getCode());
        group.setSortOrder(cmd.getSortOrder());
        group.setStatus(resolveUpdateStatus(cmd.getStatus(), group.getStatus()));
        jobTagMapper.updateGroup(group);
        return toGroupVO(group);
    }

    @Override
    @Transactional
    public void deleteGroup(Long id) {
        jobTagMapper.findGroupById(id)
                .orElseThrow(() -> new BusinessException("JobTagGroup not found: " + id));
        jobTagMapper.disableGroup(id);
        jobTagMapper.disableTagsByGroupId(id);
    }

    @Override
    public JobTagVO createTag(JobTagCmd cmd) {
        JobTag tag = new JobTag();
        tag.setGroupId(cmd.getGroupId());
        tag.setName(cmd.getName());
        tag.setCode(cmd.getCode());
        tag.setSortOrder(cmd.getSortOrder());
        tag.setStatus(normalizeStatus(cmd.getStatus()));
        jobTagMapper.insertTag(tag);
        return toTagVO(tag);
    }

    @Override
    public JobTagVO updateTag(Long id, JobTagCmd cmd) {
        JobTag tag = jobTagMapper.findTagById(id)
                .orElseThrow(() -> new BusinessException("JobTag not found: " + id));
        tag.setGroupId(cmd.getGroupId());
        tag.setName(cmd.getName());
        tag.setCode(cmd.getCode());
        tag.setSortOrder(cmd.getSortOrder());
        tag.setStatus(resolveUpdateStatus(cmd.getStatus(), tag.getStatus()));
        jobTagMapper.updateTag(tag);
        return toTagVO(tag);
    }

    @Override
    public void deleteTag(Long id) {
        jobTagMapper.findTagById(id)
                .orElseThrow(() -> new BusinessException("JobTag not found: " + id));
        jobTagMapper.disableTag(id);
    }

    private Comparator<JobTagGroup> groupComparator() {
        return Comparator.comparing(JobTagGroup::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(JobTagGroup::getId, Comparator.nullsLast(Long::compareTo));
    }

    private Comparator<JobTag> tagComparator() {
        return Comparator.comparing(JobTag::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(JobTag::getId, Comparator.nullsLast(Long::compareTo));
    }

    private String normalizeStatus(String status) {
        return status == null || status.isBlank() ? ACTIVE : status;
    }

    private String resolveUpdateStatus(String requestStatus, String existingStatus) {
        return requestStatus == null || requestStatus.isBlank() ? existingStatus : requestStatus;
    }

    private JobTagGroupVO toGroupVO(JobTagGroup group) {
        JobTagGroupVO vo = new JobTagGroupVO();
        vo.setId(group.getId());
        vo.setName(group.getName());
        vo.setCode(group.getCode());
        vo.setSortOrder(group.getSortOrder());
        vo.setStatus(group.getStatus());
        vo.setCreatedAt(group.getCreatedAt());
        vo.setUpdatedAt(group.getUpdatedAt());
        return vo;
    }

    private JobTagVO toTagVO(JobTag tag) {
        JobTagVO vo = new JobTagVO();
        vo.setId(tag.getId());
        vo.setGroupId(tag.getGroupId());
        vo.setName(tag.getName());
        vo.setCode(tag.getCode());
        vo.setSortOrder(tag.getSortOrder());
        vo.setStatus(tag.getStatus());
        vo.setCreatedAt(tag.getCreatedAt());
        vo.setUpdatedAt(tag.getUpdatedAt());
        return vo;
    }
}
