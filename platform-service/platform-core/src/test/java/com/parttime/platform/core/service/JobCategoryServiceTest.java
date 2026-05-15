package com.parttime.platform.core.service;

import com.parttime.platform.api.dto.JobCategoryRequest;
import com.parttime.platform.api.dto.JobCategoryResponse;
import com.parttime.platform.core.domain.JobCategory;
import com.parttime.platform.core.exception.BusinessException;
import com.parttime.platform.core.repository.JobCategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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
    private JobCategoryRepository jobCategoryRepository;

    @InjectMocks
    private JobCategoryService jobCategoryService;

    @Captor
    private ArgumentCaptor<JobCategory> categoryCaptor;

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

        when(jobCategoryRepository.findAll()).thenReturn(List.of(parent, child));

        List<JobCategoryResponse> result = jobCategoryService.getAllCategories();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Parent");
        assertThat(result.get(0).getChildren()).hasSize(1);
        assertThat(result.get(0).getChildren().get(0).getName()).isEqualTo("Child");
    }

    @Test
    void createCategory_shouldCreateAndReturn() {
        JobCategoryRequest request = new JobCategoryRequest();
        request.setName("New Category");
        request.setParentId(null);
        request.setSortOrder(1);

        doAnswer(invocation -> {
            JobCategory cat = invocation.getArgument(0);
            cat.setId(100L);
            return null;
        }).when(jobCategoryRepository).save(any(JobCategory.class));

        JobCategoryResponse response = jobCategoryService.createCategory(request);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getName()).isEqualTo("New Category");
        verify(jobCategoryRepository).save(any(JobCategory.class));
    }

    @Test
    void updateCategory_shouldModifyAndReturn() {
        JobCategory existing = new JobCategory();
        existing.setId(1L);
        existing.setName("Old Name");
        existing.setSortOrder(1);

        JobCategoryRequest request = new JobCategoryRequest();
        request.setName("New Name");
        request.setSortOrder(2);

        when(jobCategoryRepository.findById(1L)).thenReturn(Optional.of(existing));

        JobCategoryResponse response = jobCategoryService.updateCategory(1L, request);

        assertThat(response.getName()).isEqualTo("New Name");
        verify(jobCategoryRepository).update(existing);
    }

    @Test
    void updateCategory_notFound_shouldThrow() {
        when(jobCategoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobCategoryService.updateCategory(99L, new JobCategoryRequest()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void deleteCategory_shouldDelete() {
        jobCategoryService.deleteCategory(1L);
        verify(jobCategoryRepository).delete(1L);
    }
}
