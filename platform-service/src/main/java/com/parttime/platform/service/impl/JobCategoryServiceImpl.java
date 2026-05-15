package com.parttime.platform.service.impl;

import com.parttime.platform.exception.BusinessException;
import com.parttime.platform.mapper.JobCategoryMapper;
import com.parttime.platform.pojo.cmd.JobCategoryCmd;
import com.parttime.platform.pojo.entity.JobCategory;
import com.parttime.platform.pojo.vo.JobCategoryVO;
import com.parttime.platform.service.JobCategoryService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class JobCategoryServiceImpl implements JobCategoryService {

    @Resource
    private JobCategoryMapper jobCategoryMapper;

    @Override
    public List<JobCategoryVO> getAllCategories() {
        List<JobCategory> all = jobCategoryMapper.findAll();
        Map<Long, List<JobCategory>> byParent = all.stream()
                .collect(Collectors.groupingBy(c -> c.getParentId() != null ? c.getParentId() : 0L));
        return buildTree(byParent.getOrDefault(0L, List.of()), byParent);
    }

    private List<JobCategoryVO> buildTree(List<JobCategory> roots, Map<Long, List<JobCategory>> byParent) {
        List<JobCategoryVO> result = new ArrayList<>();
        for (JobCategory cat : roots) {
            JobCategoryVO resp = toVO(cat);
            List<JobCategory> children = byParent.get(cat.getId());
            if (children != null && !children.isEmpty()) {
                resp.setChildren(buildTree(children, byParent));
            }
            result.add(resp);
        }
        return result;
    }

    @Override
    public JobCategoryVO createCategory(JobCategoryCmd cmd) {
        JobCategory cat = new JobCategory();
        cat.setName(cmd.getName());
        cat.setParentId(cmd.getParentId());
        cat.setSortOrder(cmd.getSortOrder());
        jobCategoryMapper.insert(cat);
        return toVO(cat);
    }

    @Override
    public JobCategoryVO updateCategory(Long id, JobCategoryCmd cmd) {
        JobCategory cat = jobCategoryMapper.findById(id)
                .orElseThrow(() -> new BusinessException("JobCategory not found: " + id));
        cat.setName(cmd.getName());
        cat.setParentId(cmd.getParentId());
        cat.setSortOrder(cmd.getSortOrder());
        jobCategoryMapper.update(cat);
        return toVO(cat);
    }

    @Override
    public void deleteCategory(Long id) {
        jobCategoryMapper.delete(id);
    }

    private JobCategoryVO toVO(JobCategory cat) {
        JobCategoryVO vo = new JobCategoryVO();
        vo.setId(cat.getId());
        vo.setName(cat.getName());
        vo.setParentId(cat.getParentId());
        vo.setSortOrder(cat.getSortOrder());
        vo.setCreatedAt(cat.getCreatedAt());
        vo.setUpdatedAt(cat.getUpdatedAt());
        return vo;
    }
}
