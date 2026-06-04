package com.parttime.cservice.service.impl;

import com.parttime.cservice.mapper.JobCategoryMapper;
import com.parttime.cservice.pojo.entity.JobCategory;
import com.parttime.cservice.pojo.vo.JobCategoryVO;
import com.parttime.cservice.service.JobCategoryService;
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
    public List<JobCategoryVO> getActiveCategoryTree() {
        List<JobCategory> all = jobCategoryMapper.findActive();
        Map<Long, List<JobCategory>> byParent = all.stream()
                .collect(Collectors.groupingBy(c -> c.getParentId() != null ? c.getParentId() : 0L));
        return buildTree(byParent.getOrDefault(0L, List.of()), byParent);
    }

    private List<JobCategoryVO> buildTree(List<JobCategory> roots, Map<Long, List<JobCategory>> byParent) {
        List<JobCategoryVO> result = new ArrayList<>();
        for (JobCategory cat : roots) {
            JobCategoryVO vo = toVO(cat);
            List<JobCategory> children = byParent.get(cat.getId());
            if (children != null && !children.isEmpty()) {
                vo.setChildren(buildTree(children, byParent));
            }
            result.add(vo);
        }
        return result;
    }

    private JobCategoryVO toVO(JobCategory cat) {
        JobCategoryVO vo = new JobCategoryVO();
        vo.setId(cat.getId());
        vo.setName(cat.getName());
        vo.setParentId(cat.getParentId());
        vo.setSortOrder(cat.getSortOrder());
        vo.setStatus(cat.getStatus());
        return vo;
    }
}
