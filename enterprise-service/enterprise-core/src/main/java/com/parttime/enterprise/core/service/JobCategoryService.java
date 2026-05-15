package com.parttime.enterprise.core.service;

import com.parttime.enterprise.api.dto.JobCategoryRequest;
import com.parttime.enterprise.api.dto.JobCategoryResponse;
import com.parttime.enterprise.core.domain.JobCategory;
import com.parttime.enterprise.core.repository.JobRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JobCategoryService {

    private final JobRepository jobRepository;

    public JobCategoryService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    public List<JobCategoryResponse> getAllCategories() {
        List<JobCategory> all = jobRepository.findAllCategories();
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

    public JobCategoryResponse getCategoryById(Long id) {
        JobCategory cat = jobRepository.findCategoryById(id)
                .orElseThrow(() -> new RuntimeException("JobCategory not found: " + id));
        return toResponse(cat);
    }

    public JobCategoryResponse createCategory(JobCategoryRequest request) {
        JobCategory cat = new JobCategory();
        cat.setName(request.getName());
        cat.setParentId(request.getParentId());
        cat.setSortOrder(request.getSortOrder());
        jobRepository.saveCategory(cat);
        return toResponse(cat);
    }

    public JobCategoryResponse updateCategory(Long id, JobCategoryRequest request) {
        JobCategory cat = jobRepository.findCategoryById(id)
                .orElseThrow(() -> new RuntimeException("JobCategory not found: " + id));
        cat.setName(request.getName());
        cat.setParentId(request.getParentId());
        cat.setSortOrder(request.getSortOrder());
        jobRepository.updateCategory(cat);
        return toResponse(cat);
    }

    public void deleteCategory(Long id) {
        jobRepository.deleteCategory(id);
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