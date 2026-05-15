package com.parttime.platform.service;

import com.parttime.platform.exception.BusinessException;
import com.parttime.platform.mapper.JobCategoryMapper;
import com.parttime.platform.pojo.cmd.JobCategoryCmd;
import com.parttime.platform.pojo.entity.JobCategory;
import com.parttime.platform.pojo.vo.JobCategoryVO;
import com.parttime.platform.service.impl.JobCategoryServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobCategoryServiceTest {

    @Mock
    private JobCategoryMapper jobCategoryMapper;

    @InjectMocks
    private JobCategoryServiceImpl jobCategoryService;

    @Test
    void getAllCategories_shouldReturnTreeStructure() {
        JobCategory parent = new JobCategory();
        parent.setId(1L);
        parent.setName("Parent");
        parent.setParentId(null);
        parent.setSortOrder(1);

        JobCategory child = new JobCategory();
        child.setId(2L);
        child.setName("Child");
        child.setParentId(1L);
        child.setSortOrder(1);

        when(jobCategoryMapper.findAll()).thenReturn(List.of(parent, child));

        List<JobCategoryVO> result = jobCategoryService.getAllCategories();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Parent");
        assertThat(result.get(0).getChildren()).hasSize(1);
        assertThat(result.get(0).getChildren().get(0).getName()).isEqualTo("Child");
    }

    @Test
    void createCategory_shouldCreateAndReturn() {
        JobCategoryCmd cmd = new JobCategoryCmd();
        cmd.setName("New Category");
        cmd.setParentId(null);
        cmd.setSortOrder(1);

        doAnswer(invocation -> {
            JobCategory cat = invocation.getArgument(0);
            cat.setId(100L);
            return 1;
        }).when(jobCategoryMapper).insert(any(JobCategory.class));

        JobCategoryVO response = jobCategoryService.createCategory(cmd);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getName()).isEqualTo("New Category");
        verify(jobCategoryMapper).insert(any(JobCategory.class));
    }

    @Test
    void updateCategory_shouldModifyAndReturn() {
        JobCategory existing = new JobCategory();
        existing.setId(1L);
        existing.setName("Old Name");
        existing.setSortOrder(1);

        JobCategoryCmd cmd = new JobCategoryCmd();
        cmd.setName("New Name");
        cmd.setSortOrder(2);

        when(jobCategoryMapper.findById(1L)).thenReturn(Optional.of(existing));

        JobCategoryVO response = jobCategoryService.updateCategory(1L, cmd);

        assertThat(response.getName()).isEqualTo("New Name");
        verify(jobCategoryMapper).update(existing);
    }

    @Test
    void updateCategory_notFound_shouldThrow() {
        when(jobCategoryMapper.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobCategoryService.updateCategory(99L, new JobCategoryCmd()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void deleteCategory_shouldDelete() {
        jobCategoryService.deleteCategory(1L);
        verify(jobCategoryMapper).delete(1L);
    }
}
