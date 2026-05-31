package com.parttime.cservice.service;

import com.parttime.cservice.service.impl.ProfileServiceImpl;
import com.parttime.cservice.pojo.vo.ProfileCompletenessVO;
import com.parttime.cservice.pojo.vo.ProfileVO;
import com.parttime.cservice.pojo.cmd.ProfileUpdateCmd;
import com.parttime.cservice.pojo.vo.ResumeVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Test
    void getProfile_shouldAutoCreateWhenMissing() {
        ProfileVO response = profileService.getProfile(42L);

        assertThat(response).isNotNull();
        assertThat(response.getWorkerId()).isEqualTo(42L);
    }

    @Test
    void updateProfile_shouldPersistGenderAndBirthday() {
        ProfileUpdateCmd request = new ProfileUpdateCmd();
        request.setName("Alice");
        request.setPhone("13900000000");
        request.setGender("FEMALE");
        request.setBirthday(LocalDate.of(1995, 6, 1));

        ProfileVO response = profileService.updateProfile(1L, request);

        assertThat(response.getGender()).isEqualTo("FEMALE");
        assertThat(response.getBirthday()).isEqualTo(LocalDate.of(1995, 6, 1));

        ProfileVO fetched = profileService.getProfile(1L);
        assertThat(fetched.getGender()).isEqualTo("FEMALE");
        assertThat(fetched.getBirthday()).isEqualTo(LocalDate.of(1995, 6, 1));
    }

    @Test
    void getCompleteness_shouldReturnFalseWithMissingFieldsWhenProfileEmpty() {
        ProfileCompletenessVO comp = profileService.getCompleteness(7L);

        assertThat(comp.isComplete()).isFalse();
        assertThat(comp.getMissing()).contains("name", "phone", "gender", "birthday");
    }

    @Test
    void getCompleteness_shouldReturnFalseWhenSomeFieldsMissing() {
        ProfileUpdateCmd request = new ProfileUpdateCmd();
        request.setName("Bob");
        request.setPhone("13800000000");
        profileService.updateProfile(8L, request);

        ProfileCompletenessVO comp = profileService.getCompleteness(8L);

        assertThat(comp.isComplete()).isFalse();
        assertThat(comp.getMissing()).containsExactlyInAnyOrder("gender", "birthday");
    }

    @Test
    void getCompleteness_shouldReturnTrueWhenAllRequiredFieldsPresent() {
        ProfileUpdateCmd request = new ProfileUpdateCmd();
        request.setName("Bob");
        request.setPhone("13800000000");
        request.setGender("MALE");
        request.setBirthday(LocalDate.of(1990, 1, 1));
        profileService.updateProfile(9L, request);

        ProfileCompletenessVO comp = profileService.getCompleteness(9L);

        assertThat(comp.isComplete()).isTrue();
        assertThat(comp.getMissing()).isEmpty();
    }
}
