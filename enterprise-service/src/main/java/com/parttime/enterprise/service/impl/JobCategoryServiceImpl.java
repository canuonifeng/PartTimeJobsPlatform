package com.parttime.enterprise.service.impl;

import com.parttime.enterprise.mapper.JobCategoryMapper;
import com.parttime.enterprise.pojo.cmd.JobCategoryCmd;
import com.parttime.enterprise.pojo.entity.JobCategory;
import com.parttime.enterprise.pojo.vo.JobCategoryVO;
import com.parttime.enterprise.service.JobCategoryService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class JobCategoryServiceImpl implements JobCategoryService {

    private final JobCategoryMapper jobCategoryMapper;

    public JobCategoryServiceImpl(JobCategoryMapper jobCategoryMapper) {
        this.jobCategoryMapper = jobCategoryMapper;
    }

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
            JobCategoryVO resp = toResponse(cat);
            List<JobCategory> children = byParent.get(cat.getId());
            if (children != null && !children.isEmpty()) {
                resp.setChildren(buildTree(children, byParent));
            }
            result.add(resp);
        }
        return result;
    }

    @Override
    public JobCategoryVO getCategoryById(Long id) {
        JobCategory cat = jobCategoryMapper.findById(id)
                .orElseThrow(() -> new RuntimeException("JobCategory not found: " + id));
        return toResponse(cat);
    }

    @Override
    public JobCategoryVO createCategory(JobCategoryCmd request) {
        JobCategory cat = new JobCategory();
        cat.setName(request.getName());
        cat.setParentId(request.getParentId());
        cat.setSortOrder(request.getSortOrder());
        jobCategoryMapper.insert(cat);
        return toResponse(cat);
    }

    @Override
    public JobCategoryVO updateCategory(Long id, JobCategoryCmd request) {
        JobCategory cat = jobCategoryMapper.findById(id)
                .orElseThrow(() -> new RuntimeException("JobCategory not found: " + id));
        cat.setName(request.getName());
        cat.setParentId(request.getParentId());
        cat.setSortOrder(request.getSortOrder());
        jobCategoryMapper.update(cat);
        return toResponse(cat);
    }

    @Override
    public void deleteCategory(Long id) {
        jobCategoryMapper.delete(id);
    }

    private JobCategoryVO toResponse(JobCategory cat) {
        JobCategoryVO resp = new JobCategoryVO();
        resp.setId(cat.getId());
        resp.setName(cat.getName());
        resp.setParentId(cat.getParentId());
        resp.setSortOrder(cat.getSortOrder());
        resp.setCreatedAt(cat.getCreatedAt());
        resp.setUpdatedAt(cat.getUpdatedAt());
        return resp;
    }
}
