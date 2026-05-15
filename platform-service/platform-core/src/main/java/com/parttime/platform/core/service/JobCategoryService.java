package com.parttime.platform.core.service;

import com.parttime.platform.api.dto.JobCategoryRequest;
import com.parttime.platform.api.dto.JobCategoryResponse;
import com.parttime.platform.core.domain.JobCategory;
import com.parttime.platform.core.exception.BusinessException;
import com.parttime.platform.core.repository.JobCategoryRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JobCategoryService {

    private final JobCategoryRepository jobCategoryRepository;

    public JobCategoryService(JobCategoryRepository jobCategoryRepository) {
        this.jobCategoryRepository = jobCategoryRepository;
    }

    public List<JobCategoryResponse> getAllCategories() {
        List<JobCategory> all = jobCategoryRepository.findAll();
        Map<Long, List<JobCategory>> byParent = all.stream()
                .collect(Collectors.groupingBy(c -> c.getParentId() != null ? c.getParentId() : 0L));
        return buildTree(byParent.getOrDefault(0L, List.of()), byParent);
    }

    private List<JobCategoryResponse> buildTree(List<JobCategory> roots, Map<Long, List<JobCategory>> byParent) {
        List<JobCategoryResponse> result = new ArrayList<>();
        for (JobCategory cat : roots) {
            JobCategoryResponse resp = toResponse(cat);
            List<JobCategory> children = byParent.get(cat.getId());
            if (children != null && !children.isEmpty()) {
                resp.setChildren(buildTree(children, byParent));
            }
            result.add(resp);
        }
        return result;
    }

    public JobCategoryResponse createCategory(JobCategoryRequest request) {
        JobCategory cat = new JobCategory();
        cat.setName(request.getName());
        cat.setParentId(request.getParentId());
        cat.setSortOrder(request.getSortOrder());
        jobCategoryRepository.save(cat);
        return toResponse(cat);
    }

    public JobCategoryResponse updateCategory(Long id, JobCategoryRequest request) {
        JobCategory cat = jobCategoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException("JobCategory not found: " + id));
        cat.setName(request.getName());
        cat.setParentId(request.getParentId());
        cat.setSortOrder(request.getSortOrder());
        jobCategoryRepository.update(cat);
        return toResponse(cat);
    }

    public void deleteCategory(Long id) {
        jobCategoryRepository.delete(id);
    }

    private JobCategoryResponse toResponse(JobCategory cat) {
        JobCategoryResponse resp = new JobCategoryResponse();
        resp.setId(cat.getId());
        resp.setName(cat.getName());
        resp.setParentId(cat.getParentId());
        resp.setSortOrder(cat.getSortOrder());
        resp.setCreatedAt(cat.getCreatedAt());
        resp.setUpdatedAt(cat.getUpdatedAt());
        return resp;
    }
}
