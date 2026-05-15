package com.parttime.cservice.core.service;

import com.parttime.cservice.core.dto.ProfileResponse;
import com.parttime.cservice.core.dto.ProfileUpdateRequest;
import com.parttime.cservice.core.dto.ResumeResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProfileServiceTest {

    private ProfileService profileService;

    @BeforeEach
    void setUp() {
        profileService = new ProfileService();
    }

    @Test
    void updateProfile_shouldCreateNewProfile() {
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setName("John");
        request.setPhone("13800138000");
        request.setAvatarUrl("http://avatar.url");
        request.setSkills("welding, plumbing");
        request.setAvailableDays("[1,2,3,4,5]");

        ProfileResponse response = profileService.updateProfile(1L, request);

        assertThat(response.getWorkerId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("John");
        assertThat(response.getPhone()).isEqualTo("13800138000");
        assertThat(response.getAvatarUrl()).isEqualTo("http://avatar.url");
        assertThat(response.getSkills()).isEqualTo("welding, plumbing");
        assertThat(response.getAvailableDays()).isEqualTo("[1,2,3,4,5]");
        assertThat(response.getCreatedAt()).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();
    }

    @Test
    void updateProfile_shouldUpsertExistingProfile() {
        ProfileUpdateRequest request1 = new ProfileUpdateRequest();
        request1.setName("John");
        request1.setPhone("13800138000");
        profileService.updateProfile(1L, request1);

        ProfileUpdateRequest request2 = new ProfileUpdateRequest();
        request2.setName("John Updated");
        ProfileResponse response = profileService.updateProfile(1L, request2);

        assertThat(response.getName()).isEqualTo("John Updated");
        assertThat(response.getPhone()).isEqualTo("13800138000");
    }

    @Test
    void getProfile_shouldReturnProfile() {
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setName("John");
        profileService.updateProfile(1L, request);

        ProfileResponse response = profileService.getProfile(1L);

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
        ResumeResponse response = profileService.uploadResume(1L, "resume.pdf", "http://files/resume.pdf");

        assertThat(response.getWorkerId()).isEqualTo(1L);
        assertThat(response.getFileName()).isEqualTo("resume.pdf");
        assertThat(response.getFileUrl()).isEqualTo("http://files/resume.pdf");
        assertThat(response.getUploadedAt()).isNotNull();
    }

    @Test
    void getResumes_shouldReturnList() {
        profileService.uploadResume(1L, "resume1.pdf", "http://files/1.pdf");
        profileService.uploadResume(1L, "resume2.pdf", "http://files/2.pdf");

        List<ResumeResponse> responses = profileService.getResumes(1L);

        assertThat(responses).hasSize(2);
    }

    @Test
    void getResumes_shouldReturnEmptyWhenNoResumes() {
        List<ResumeResponse> responses = profileService.getResumes(999L);
        assertThat(responses).isEmpty();
    }

    @Test
    void uploadResume_shouldGenerateSequentialIds() {
        ResumeResponse r1 = profileService.uploadResume(1L, "a.pdf", "http://a");
        ResumeResponse r2 = profileService.uploadResume(1L, "b.pdf", "http://b");

        assertThat(r2.getId()).isGreaterThan(r1.getId());
    }
}
