package com.parttime.enterprise.core.service;

import com.parttime.enterprise.api.dto.*;
import com.parttime.enterprise.core.domain.*;
import com.parttime.enterprise.core.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkerProfileServiceTest {

    @Mock
    private WorkerBlacklistRepository blacklistRepository;

    @Mock
    private WorkerEvaluationRepository evaluationRepository;

    @Mock
    private ScheduleShiftRepository shiftRepository;

    @Mock
    private JobRepository jobRepository;

    private WorkerProfileService workerProfileService;

    @BeforeEach
    void setUp() {
        workerProfileService = new WorkerProfileService(
                blacklistRepository, evaluationRepository, shiftRepository, jobRepository);
    }

    @Test
    void getWorkerProfile_shouldReturnProfileWithAllFields() {
        when(blacklistRepository.findByCompanyIdAndWorkerId(1L, 10L)).thenReturn(Optional.empty());
        when(evaluationRepository.findAvgRatingByWorkerIdAndCompanyId(10L, 1L)).thenReturn(4.5);
        when(evaluationRepository.findByWorkerIdAndCompanyId(10L, 1L)).thenReturn(List.of(new WorkerEvaluation(), new WorkerEvaluation()));
        when(shiftRepository.findCompletedByWorkerIdAndCompanyId(10L, 1L)).thenReturn(List.of());

        WorkerProfileResponse profile = workerProfileService.getWorkerProfile(1L, 10L);

        assertThat(profile.getWorkerId()).isEqualTo(10L);
        assertThat(profile.getAvgRating()).isEqualTo(4.5);
        assertThat(profile.getTotalEvaluations()).isEqualTo(2);
        assertThat(profile.getIsBlacklisted()).isFalse();
        assertThat(profile.getWorkHistory()).isEmpty();
    }

    @Test
    void getWorkerProfile_shouldIncludeBlacklistInfoWhenBlacklisted() {
        WorkerBlacklist blacklist = new WorkerBlacklist();
        blacklist.setCompanyId(1L);
        blacklist.setWorkerId(10L);
        blacklist.setReason("No-show");

        when(blacklistRepository.findByCompanyIdAndWorkerId(1L, 10L))
                .thenReturn(Optional.of(blacklist))
                .thenReturn(Optional.of(blacklist));
        when(evaluationRepository.findAvgRatingByWorkerIdAndCompanyId(10L, 1L)).thenReturn(null);
        when(evaluationRepository.findByWorkerIdAndCompanyId(10L, 1L)).thenReturn(List.of());
        when(shiftRepository.findCompletedByWorkerIdAndCompanyId(10L, 1L)).thenReturn(List.of());

        WorkerProfileResponse profile = workerProfileService.getWorkerProfile(1L, 10L);

        assertThat(profile.getIsBlacklisted()).isTrue();
        assertThat(profile.getBlacklistReason()).isEqualTo("No-show");
        assertThat(profile.getAvgRating()).isNull();
        assertThat(profile.getTotalEvaluations()).isEqualTo(0);
    }

    @Test
    void getWorkerProfile_shouldIncludeWorkHistory() {
        ScheduleShift shift = new ScheduleShift();
        shift.setId(100L);
        shift.setJobId(50L);
        shift.setWorkerId(10L);
        shift.setShiftDate(LocalDate.of(2026, 5, 1));
        shift.setStartTime(LocalTime.of(9, 0));
        shift.setEndTime(LocalTime.of(18, 0));

        Job job = new Job();
        job.setId(50L);
        job.setTitle("Warehouse Helper");

        when(blacklistRepository.findByCompanyIdAndWorkerId(1L, 10L)).thenReturn(Optional.empty());
        when(evaluationRepository.findAvgRatingByWorkerIdAndCompanyId(10L, 1L)).thenReturn(4.0);
        when(evaluationRepository.findByWorkerIdAndCompanyId(10L, 1L)).thenReturn(List.of());
        when(shiftRepository.findCompletedByWorkerIdAndCompanyId(10L, 1L)).thenReturn(List.of(shift));
        when(jobRepository.findById(50L)).thenReturn(Optional.of(job));

        WorkerProfileResponse profile = workerProfileService.getWorkerProfile(1L, 10L);

        assertThat(profile.getWorkHistory()).hasSize(1);
        assertThat(profile.getWorkHistory().get(0).getJobTitle()).isEqualTo("Warehouse Helper");
        assertThat(profile.getWorkHistory().get(0).getShiftId()).isEqualTo(100L);
    }

    @Test
    void evaluateWorker_shouldCreateEvaluation() {
        doAnswer(invocation -> {
            WorkerEvaluation e = invocation.getArgument(0);
            e.setId(99L);
            return null;
        }).when(evaluationRepository).save(any(WorkerEvaluation.class));

        EvaluationResponse response = workerProfileService.evaluateWorker(1L, 50L, 10L, 5, "Excellent worker");

        assertThat(response.getId()).isEqualTo(99L);
        assertThat(response.getCompanyId()).isEqualTo(1L);
        assertThat(response.getJobId()).isEqualTo(50L);
        assertThat(response.getWorkerId()).isEqualTo(10L);
        assertThat(response.getRating()).isEqualTo(5);
        assertThat(response.getComment()).isEqualTo("Excellent worker");

        verify(evaluationRepository).save(any(WorkerEvaluation.class));
    }

    @Test
    void evaluateWorker_shouldThrowWhenRatingOutOfRange() {
        assertThatThrownBy(() -> workerProfileService.evaluateWorker(1L, 50L, 10L, 0, "bad"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> workerProfileService.evaluateWorker(1L, 50L, 10L, 6, "bad"))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(evaluationRepository);
    }

    @Test
    void addToBlacklist_shouldSave() {
        workerProfileService.addToBlacklist(1L, 10L, "Frequent no-show");

        verify(blacklistRepository).save(any(WorkerBlacklist.class));
    }

    @Test
    void removeFromBlacklist_shouldDelete() {
        workerProfileService.removeFromBlacklist(1L, 10L);

        verify(blacklistRepository).deleteByCompanyIdAndWorkerId(1L, 10L);
    }

    @Test
    void isBlacklisted_shouldReturnTrueWhenFound() {
        when(blacklistRepository.findByCompanyIdAndWorkerId(1L, 10L))
                .thenReturn(Optional.of(new WorkerBlacklist()));

        assertThat(workerProfileService.isBlacklisted(1L, 10L)).isTrue();
    }

    @Test
    void isBlacklisted_shouldReturnFalseWhenNotFound() {
        when(blacklistRepository.findByCompanyIdAndWorkerId(1L, 10L))
                .thenReturn(Optional.empty());

        assertThat(workerProfileService.isBlacklisted(1L, 10L)).isFalse();
    }

    @Test
    void getWorkHistory_shouldReturnCompletedShifts() {
        ScheduleShift shift1 = new ScheduleShift();
        shift1.setId(100L);
        shift1.setJobId(50L);
        shift1.setWorkerId(10L);
        shift1.setShiftDate(LocalDate.of(2026, 5, 1));
        shift1.setStartTime(LocalTime.of(9, 0));
        shift1.setEndTime(LocalTime.of(18, 0));

        ScheduleShift shift2 = new ScheduleShift();
        shift2.setId(101L);
        shift2.setJobId(51L);
        shift2.setWorkerId(10L);
        shift2.setShiftDate(LocalDate.of(2026, 5, 2));
        shift2.setStartTime(LocalTime.of(10, 0));
        shift2.setEndTime(LocalTime.of(17, 0));

        when(shiftRepository.findCompletedByWorkerIdAndCompanyId(10L, 1L))
                .thenReturn(List.of(shift1, shift2));
        when(jobRepository.findById(50L)).thenReturn(Optional.empty());
        when(jobRepository.findById(51L)).thenReturn(Optional.empty());

        List<WorkHistoryResponse> history = workerProfileService.getWorkHistory(10L, 1L);

        assertThat(history).hasSize(2);
        assertThat(history.get(0).getShiftId()).isEqualTo(100L);
        assertThat(history.get(1).getShiftId()).isEqualTo(101L);
    }

    @Test
    void getEvaluations_shouldReturnList() {
        WorkerEvaluation e1 = new WorkerEvaluation();
        e1.setId(1L);
        e1.setCompanyId(1L);
        e1.setJobId(50L);
        e1.setWorkerId(10L);
        e1.setRating(4);
        e1.setComment("Good");

        when(evaluationRepository.findByWorkerIdAndCompanyId(10L, 1L)).thenReturn(List.of(e1));

        List<EvaluationResponse> responses = workerProfileService.getEvaluations(10L, 1L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getRating()).isEqualTo(4);
        assertThat(responses.get(0).getComment()).isEqualTo("Good");
    }
}
