package com.parttime.enterprise.service;

import com.parttime.enterprise.mapper.JobMapper;
import com.parttime.enterprise.mapper.ScheduleShiftMapper;
import com.parttime.enterprise.mapper.WorkerBlacklistMapper;
import com.parttime.enterprise.mapper.WorkerEvaluationMapper;
import com.parttime.enterprise.pojo.entity.Job;
import com.parttime.enterprise.pojo.entity.ScheduleShift;
import com.parttime.enterprise.pojo.entity.WorkerBlacklist;
import com.parttime.enterprise.pojo.entity.WorkerEvaluation;
import com.parttime.enterprise.pojo.vo.EvaluationVO;
import com.parttime.enterprise.pojo.vo.WorkHistoryVO;
import com.parttime.enterprise.pojo.vo.WorkerProfileVO;
import com.parttime.enterprise.service.impl.WorkerProfileServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkerProfileServiceTest {

    @Mock
    private WorkerBlacklistMapper blacklistMapper;

    @Mock
    private WorkerEvaluationMapper evaluationMapper;

    @Mock
    private ScheduleShiftMapper shiftMapper;

    @Mock
    private JobMapper jobMapper;

    @InjectMocks
    private WorkerProfileServiceImpl workerProfileService;

    @Test
    void getWorkerProfile_shouldReturnProfileWithAllFields() {
        when(blacklistMapper.findByCompanyIdAndWorkerId(1L, 10L)).thenReturn(Optional.empty());
        when(evaluationMapper.findAvgRatingByWorkerIdAndCompanyId(10L, 1L)).thenReturn(4.5);
        when(evaluationMapper.findByWorkerIdAndCompanyId(10L, 1L)).thenReturn(List.of(new WorkerEvaluation(), new WorkerEvaluation()));
        when(shiftMapper.findCompletedByWorkerIdAndCompanyId(10L, 1L)).thenReturn(List.of());

        WorkerProfileVO profile = workerProfileService.getWorkerProfile(1L, 10L);

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

        when(blacklistMapper.findByCompanyIdAndWorkerId(1L, 10L))
                .thenReturn(Optional.of(blacklist))
                .thenReturn(Optional.of(blacklist));
        when(evaluationMapper.findAvgRatingByWorkerIdAndCompanyId(10L, 1L)).thenReturn(null);
        when(evaluationMapper.findByWorkerIdAndCompanyId(10L, 1L)).thenReturn(List.of());
        when(shiftMapper.findCompletedByWorkerIdAndCompanyId(10L, 1L)).thenReturn(List.of());

        WorkerProfileVO profile = workerProfileService.getWorkerProfile(1L, 10L);

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

        when(blacklistMapper.findByCompanyIdAndWorkerId(1L, 10L)).thenReturn(Optional.empty());
        when(evaluationMapper.findAvgRatingByWorkerIdAndCompanyId(10L, 1L)).thenReturn(4.0);
        when(evaluationMapper.findByWorkerIdAndCompanyId(10L, 1L)).thenReturn(List.of());
        when(shiftMapper.findCompletedByWorkerIdAndCompanyId(10L, 1L)).thenReturn(List.of(shift));
        when(jobMapper.findById(50L)).thenReturn(Optional.of(job));

        WorkerProfileVO profile = workerProfileService.getWorkerProfile(1L, 10L);

        assertThat(profile.getWorkHistory()).hasSize(1);
        assertThat(profile.getWorkHistory().get(0).getJobTitle()).isEqualTo("Warehouse Helper");
        assertThat(profile.getWorkHistory().get(0).getShiftId()).isEqualTo(100L);
    }

    @Test
    void evaluateWorker_shouldCreateEvaluation() {
        doAnswer(invocation -> {
            WorkerEvaluation e = invocation.getArgument(0);
            e.setId(99L);
            return 1;
        }).when(evaluationMapper).insert(any(WorkerEvaluation.class));

        EvaluationVO response = workerProfileService.evaluateWorker(1L, 50L, 10L, 5, "Excellent worker");

        assertThat(response.getId()).isEqualTo(99L);
        assertThat(response.getCompanyId()).isEqualTo(1L);
        assertThat(response.getJobId()).isEqualTo(50L);
        assertThat(response.getWorkerId()).isEqualTo(10L);
        assertThat(response.getRating()).isEqualTo(5);
        assertThat(response.getComment()).isEqualTo("Excellent worker");

        verify(evaluationMapper).insert(any(WorkerEvaluation.class));
    }

    @Test
    void evaluateWorker_shouldThrowWhenRatingOutOfRange() {
        assertThatThrownBy(() -> workerProfileService.evaluateWorker(1L, 50L, 10L, 0, "bad"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> workerProfileService.evaluateWorker(1L, 50L, 10L, 6, "bad"))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(evaluationMapper);
    }

    @Test
    void addToBlacklist_shouldSave() {
        workerProfileService.addToBlacklist(1L, 10L, "Frequent no-show");

        verify(blacklistMapper).insert(any(WorkerBlacklist.class));
    }

    @Test
    void removeFromBlacklist_shouldDelete() {
        workerProfileService.removeFromBlacklist(1L, 10L);

        verify(blacklistMapper).deleteByCompanyIdAndWorkerId(1L, 10L);
    }

    @Test
    void isBlacklisted_shouldReturnTrueWhenFound() {
        when(blacklistMapper.findByCompanyIdAndWorkerId(1L, 10L))
                .thenReturn(Optional.of(new WorkerBlacklist()));

        assertThat(workerProfileService.isBlacklisted(1L, 10L)).isTrue();
    }

    @Test
    void isBlacklisted_shouldReturnFalseWhenNotFound() {
        when(blacklistMapper.findByCompanyIdAndWorkerId(1L, 10L))
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

        when(shiftMapper.findCompletedByWorkerIdAndCompanyId(10L, 1L))
                .thenReturn(List.of(shift1, shift2));
        when(jobMapper.findById(50L)).thenReturn(Optional.empty());
        when(jobMapper.findById(51L)).thenReturn(Optional.empty());

        List<WorkHistoryVO> history = workerProfileService.getWorkHistory(10L, 1L);

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

        when(evaluationMapper.findByWorkerIdAndCompanyId(10L, 1L)).thenReturn(List.of(e1));

        List<EvaluationVO> responses = workerProfileService.getEvaluations(10L, 1L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getRating()).isEqualTo(4);
        assertThat(responses.get(0).getComment()).isEqualTo("Good");
    }
}
