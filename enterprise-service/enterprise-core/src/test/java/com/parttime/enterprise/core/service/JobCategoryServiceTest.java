package com.parttime.enterprise.core.service;

import com.parttime.enterprise.api.dto.JobCategoryRequest;
import com.parttime.enterprise.api.dto.JobCategoryResponse;
import com.parttime.enterprise.core.domain.JobCategory;
import com.parttime.enterprise.core.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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
    private JobRepository jobRepository;

    @Captor
    private ArgumentCaptor<JobCategory> categoryCaptor;

    private JobCategoryService jobCategoryService;

    @BeforeEach
    void setUp() {
        jobCategoryService = new JobCategoryService(jobRepository);
    }

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

        when(jobRepository.findAllCategories()).thenReturn(List.of(parent, child));

        List<JobCategoryResponse> result = jobCategoryService.getAllCategories();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Parent");
        assertThat(result.get(0).getChildren()).hasSize(1);
        assertThat(result.get(0).getChildren().get(0).getName()).isEqualTo("Child");
    }

    @Test
    void getCategoryById_shouldReturnCategory() {
        JobCategory cat = new JobCategory();
        cat.setId(1L);
        cat.setName("Test");
        cat.setSortOrder(1);

        when(jobRepository.findCategoryById(1L)).thenReturn(Optional.of(cat));

        JobCategoryResponse response = jobCategoryService.getCategoryById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Test");
    }

    @Test
    void getCategoryById_shouldThrowWhenNotFound() {
        when(jobRepository.findCategoryById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobCategoryService.getCategoryById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
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
        }).when(jobRepository).saveCategory(any(JobCategory.class));

        JobCategoryResponse response = jobCategoryService.createCategory(request);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getName()).isEqualTo("New Category");
        verify(jobRepository).saveCategory(any(JobCategory.class));
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

        when(jobRepository.findCategoryById(1L)).thenReturn(Optional.of(existing));

        JobCategoryResponse response = jobCategoryService.updateCategory(1L, request);

        assertThat(response.getName()).isEqualTo("New Name");
        verify(jobRepository).updateCategory(existing);
    }

    @Test
    void deleteCategory_shouldDelete() {
        jobCategoryService.deleteCategory(1L);
        verify(jobRepository).deleteCategory(1L);
    }
}