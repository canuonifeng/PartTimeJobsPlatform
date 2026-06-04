package com.parttime.cservice.service;

import com.parttime.cservice.mapper.JobCategoryMapper;
import com.parttime.cservice.pojo.entity.JobCategory;
import com.parttime.cservice.pojo.vo.JobCategoryVO;
import com.parttime.cservice.service.impl.JobCategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JobCategoryServiceTest {

    private JobCategoryServiceImpl jobCategoryService;

    @BeforeEach
    void setUp() {
        jobCategoryService = new JobCategoryServiceImpl();
        ReflectionTestUtils.setField(jobCategoryService, "jobCategoryMapper", new JobCategoryMapper() {
            @Override
            public List<JobCategory> findActive() {
                return List.of(
                                category(3L, "零售", null, 2, "ACTIVE"),
                                category(2L, "服务员", 1L, 1, "ACTIVE"),
                                category(4L, "收银员", 1L, 1, "ACTIVE"),
                                category(1L, "餐饮", null, 1, "ACTIVE"),
                                category(5L, "停用分类", null, 3, "DISABLED")
                        ).stream()
                        .filter(category -> "ACTIVE".equals(category.getStatus()))
                        .sorted(Comparator.comparing(JobCategory::getSortOrder).thenComparing(JobCategory::getId))
                        .toList();
            }
        });
    }

    @Test
    void getActiveCategoryTree_preservesParentChildShapeAndOrdering() {
        List<JobCategoryVO> result = jobCategoryService.getActiveCategoryTree();

        assertThat(result).extracting(JobCategoryVO::getId).containsExactly(1L, 3L);
        assertThat(result).extracting(JobCategoryVO::getStatus).containsOnly("ACTIVE");
        assertThat(result.get(0).getChildren()).extracting(JobCategoryVO::getId).containsExactly(2L, 4L);
        assertThat(result.get(0).getChildren()).extracting(JobCategoryVO::getParentId).containsOnly(1L);
    }

    private JobCategory category(Long id, String name, Long parentId, Integer sortOrder, String status) {
        JobCategory category = new JobCategory();
        category.setId(id);
        category.setName(name);
        category.setParentId(parentId);
        category.setSortOrder(sortOrder);
        category.setStatus(status);
        return category;
    }
}
