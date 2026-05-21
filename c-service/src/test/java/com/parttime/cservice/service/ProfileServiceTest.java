package com.parttime.cservice.service;

import com.parttime.cservice.service.impl.ProfileServiceImpl;
import com.parttime.cservice.pojo.vo.ProfileVO;
import com.parttime.cservice.pojo.cmd.ProfileUpdateCmd;
import com.parttime.cservice.pojo.vo.ResumeVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

class ProfileServiceTest {

    @InjectMocks
    private ProfileServiceImpl profileService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(profileService, "workerProfileMapper", InMemoryMappers.createWorkerProfileMapper());
        ReflectionTestUtils.setField(profileService, "workerResumeMapper", InMemoryMappers.createWorkerResumeMapper());
    }

    @Test
    void updateProfile_shouldCreateNewProfile() {
        ProfileUpdateCmd request = new ProfileUpdateCmd();
        request.setName("John");
        request.setPhone("13800138000");
        request.setAvatarUrl("http://avatar.url");
        request.setSkills(List.of("welding", "plumbing"));
        request.setAvailableDays(List.of("Monday", "Tuesday", "Wednesday", "Thursday", "Friday"));

        ProfileVO response = profileService.updateProfile(1L, request);

        assertThat(response.getWorkerId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("John");
        assertThat(response.getPhone()).isEqualTo("13800138000");
        assertThat(response.getAvatarUrl()).isEqualTo("http://avatar.url");
        assertThat(response.getSkills()).containsExactly("welding", "plumbing");
        assertThat(response.getAvailableDays()).containsExactly("Monday", "Tuesday", "Wednesday", "Thursday", "Friday");
        assertThat(response.getCreatedAt()).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();
    }

    @Test
    void updateProfile_shouldUpsertExistingProfile() {
        ProfileUpdateCmd request1 = new ProfileUpdateCmd();
        request1.setName("John");
        request1.setPhone("13800138000");
        profileService.updateProfile(1L, request1);

        ProfileUpdateCmd request2 = new ProfileUpdateCmd();
        request2.setName("John Updated");
        ProfileVO response = profileService.updateProfile(1L, request2);

        assertThat(response.getName()).isEqualTo("John Updated");
        assertThat(response.getPhone()).isEqualTo("13800138000");
    }

    @Test
    void getProfile_shouldReturnProfile() {
        ProfileUpdateCmd request = new ProfileUpdateCmd();
        request.setName("John");
        profileService.updateProfile(1L, request);

        ProfileVO response = profileService.getProfile(1L);

        assertThat(response.getName()).isEqualTo("John");
        assertThat(response.getWorkerId()).isEqualTo(1L);
    }

    @Test
    void getProfile_shouldThrowWhenNotFound() {
        assertThatThrownBy(() -> profileService.getProfile(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void uploadResume_shouldAddResume() {
        ResumeVO response = profileService.uploadResume(1L, "resume.pdf", "http://files/resume.pdf");

        assertThat(response.getWorkerId()).isEqualTo(1L);
        assertThat(response.getFileName()).isEqualTo("resume.pdf");
        assertThat(response.getFileUrl()).isEqualTo("http://files/resume.pdf");
        assertThat(response.getUploadedAt()).isNotNull();
    }

    @Test
    void getResumes_shouldReturnList() {
        profileService.uploadResume(1L, "resume1.pdf", "http://files/1.pdf");
        profileService.uploadResume(1L, "resume2.pdf", "http://files/2.pdf");

        List<ResumeVO> responses = profileService.getResumes(1L);

        assertThat(responses).hasSize(2);
    }

    @Test
    void getResumes_shouldReturnEmptyWhenNoResumes() {
        List<ResumeVO> responses = profileService.getResumes(999L);
        assertThat(responses).isEmpty();
    }

    @Test
    void uploadResume_shouldGenerateSequentialIds() {
        ResumeVO r1 = profileService.uploadResume(1L, "a.pdf", "http://a");
        ResumeVO r2 = profileService.uploadResume(1L, "b.pdf", "http://b");

        assertThat(r2.getId()).isGreaterThan(r1.getId());
    }
}
