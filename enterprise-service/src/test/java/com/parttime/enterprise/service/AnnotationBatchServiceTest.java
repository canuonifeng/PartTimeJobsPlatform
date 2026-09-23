package com.parttime.enterprise.service;

import com.parttime.enterprise.enums.TaskType;
import com.parttime.enterprise.exception.BusinessException;
import com.parttime.enterprise.mapper.AnnotationTaskOrderMapper;
import com.parttime.enterprise.mapper.JobMapper;
import com.parttime.enterprise.mapper.JobScheduleMapper;
import com.parttime.enterprise.pojo.cmd.AnnotationBatchCreateCmd;
import com.parttime.enterprise.pojo.cmd.AnnotationBatchListCmd;
import com.parttime.enterprise.pojo.cmd.AnnotationBatchToggleCmd;
import com.parttime.enterprise.pojo.cmd.AnnotationBatchUpdateCmd;
import com.parttime.enterprise.pojo.entity.Job;
import com.parttime.enterprise.pojo.entity.JobSchedule;
import com.parttime.enterprise.pojo.vo.AnnotationBatchProgress;
import com.parttime.enterprise.pojo.vo.AnnotationBatchVO;
import com.parttime.enterprise.service.impl.AnnotationBatchServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnnotationBatchServiceTest {

    @Mock
    private JobScheduleMapper jobScheduleMapper;
    @Mock
    private AnnotationTaskOrderMapper taskOrderMapper;
    @Mock
    private JobMapper jobMapper;

    @InjectMocks
    private AnnotationBatchServiceImpl service;

    @Captor
    private ArgumentCaptor<JobSchedule> scheduleCaptor;

    private Job ownedAnnotationJob() {
        Job job = new Job();
        job.setId(10L);
        job.setCompanyId(1L);
        job.setTaskType(TaskType.ANNOTATION.getCode());
        return job;
    }

    private AnnotationBatchCreateCmd validCreateCmd() {
        AnnotationBatchCreateCmd cmd = new AnnotationBatchCreateCmd();
        cmd.setJobId(10L);
        cmd.setBatchCode("BATCH_001");
        cmd.setTotalItems(1000);
        cmd.setExternalBatchId("EXT_001");
        return cmd;
    }

    @Test
    void create_shouldPersistBatchWithNullScheduleFields() {
        when(jobMapper.findById(10L)).thenReturn(Optional.of(ownedAnnotationJob()));

        AnnotationBatchVO vo = service.create(1L, validCreateCmd());

        verify(jobScheduleMapper).insert(scheduleCaptor.capture());
        JobSchedule saved = scheduleCaptor.getValue();
        assertThat(saved.getJobId()).isEqualTo(10L);
        assertThat(saved.getBatchCode()).isEqualTo("BATCH_001");
        assertThat(saved.getTotalItems()).isEqualTo(1000);
        assertThat(saved.getExternalBatchId()).isEqualTo("EXT_001");
        assertThat(saved.getStatus()).isEqualTo("ACTIVE");
        assertThat(saved.getScheduleDate()).isNull();
        assertThat(saved.getStartTime()).isNull();
        assertThat(saved.getEndTime()).isNull();
        assertThat(saved.getSlotsAvailable()).isEqualTo(1);

        assertThat(vo.getBatchCode()).isEqualTo("BATCH_001");
        assertThat(vo.getTotalItems()).isEqualTo(1000);
        assertThat(vo.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void create_shouldTrimBatchCode() {
        when(jobMapper.findById(10L)).thenReturn(Optional.of(ownedAnnotationJob()));
        AnnotationBatchCreateCmd cmd = validCreateCmd();
        cmd.setBatchCode("  BATCH_001  ");

        service.create(1L, cmd);

        verify(jobScheduleMapper).insert(scheduleCaptor.capture());
        assertThat(scheduleCaptor.getValue().getBatchCode()).isEqualTo("BATCH_001");
    }

    @Test
    void create_nonAnnotationJob_shouldReject() {
        Job job = ownedAnnotationJob();
        job.setTaskType(TaskType.WORK.getCode());
        when(jobMapper.findById(10L)).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> service.create(1L, validCreateCmd()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("不是标注类型");
        verify(jobScheduleMapper, never()).insert(any());
    }

    @Test
    void create_blankBatchCode_shouldReject() {
        when(jobMapper.findById(10L)).thenReturn(Optional.of(ownedAnnotationJob()));
        AnnotationBatchCreateCmd cmd = validCreateCmd();
        cmd.setBatchCode("  ");

        assertThatThrownBy(() -> service.create(1L, cmd))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("批次编码不能为空");
        verify(jobScheduleMapper, never()).insert(any());
    }

    @Test
    void create_nullTotalItems_shouldReject() {
        when(jobMapper.findById(10L)).thenReturn(Optional.of(ownedAnnotationJob()));
        AnnotationBatchCreateCmd cmd = validCreateCmd();
        cmd.setTotalItems(null);

        assertThatThrownBy(() -> service.create(1L, cmd))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("总数量不能为空");
        verify(jobScheduleMapper, never()).insert(any());
    }

    @Test
    void create_nonPositiveTotalItems_shouldReject() {
        when(jobMapper.findById(10L)).thenReturn(Optional.of(ownedAnnotationJob()));
        AnnotationBatchCreateCmd cmd = validCreateCmd();
        cmd.setTotalItems(0);

        assertThatThrownBy(() -> service.create(1L, cmd))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("总数量必须大于0");
        verify(jobScheduleMapper, never()).insert(any());
    }

    @Test
    void create_jobNotOwned_shouldReject() {
        Job job = ownedAnnotationJob();
        job.setCompanyId(2L);
        when(jobMapper.findById(10L)).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> service.create(1L, validCreateCmd()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("岗位不存在");
        verify(jobScheduleMapper, never()).insert(any());
    }

    @Test
    void create_nullJobId_shouldReject() {
        AnnotationBatchCreateCmd cmd = validCreateCmd();
        cmd.setJobId(null);

        assertThatThrownBy(() -> service.create(1L, cmd))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("岗位ID不能为空");
        verify(jobScheduleMapper, never()).insert(any());
    }

    @Test
    void update_shouldUpdateBatchFields() {
        JobSchedule batch = new JobSchedule();
        batch.setId(5L);
        batch.setJobId(10L);
        batch.setBatchCode("OLD");
        batch.setTotalItems(100);
        batch.setStatus("ACTIVE");
        when(jobScheduleMapper.findById(5L)).thenReturn(Optional.of(batch));
        when(jobMapper.findById(10L)).thenReturn(Optional.of(ownedAnnotationJob()));
        when(taskOrderMapper.aggregateProgress(anyList())).thenReturn(Collections.emptyList());

        AnnotationBatchUpdateCmd cmd = new AnnotationBatchUpdateCmd();
        cmd.setId(5L);
        cmd.setBatchCode("NEW_CODE");
        cmd.setTotalItems(2000);
        cmd.setExternalBatchId("NEW_EXT");

        AnnotationBatchVO vo = service.update(1L, cmd);

        verify(jobScheduleMapper).updateBatch(5L, "NEW_CODE", 2000, "NEW_EXT");
        assertThat(vo.getBatchCode()).isEqualTo("NEW_CODE");
        assertThat(vo.getTotalItems()).isEqualTo(2000);
        assertThat(vo.getExternalBatchId()).isEqualTo("NEW_EXT");
    }

    @Test
    void update_blankBatchCode_shouldReject() {
        JobSchedule batch = new JobSchedule();
        batch.setId(5L);
        batch.setJobId(10L);
        when(jobScheduleMapper.findById(5L)).thenReturn(Optional.of(batch));
        when(jobMapper.findById(10L)).thenReturn(Optional.of(ownedAnnotationJob()));

        AnnotationBatchUpdateCmd cmd = new AnnotationBatchUpdateCmd();
        cmd.setId(5L);
        cmd.setBatchCode("  ");
        cmd.setTotalItems(100);

        assertThatThrownBy(() -> service.update(1L, cmd))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("批次编码不能为空");
        verify(jobScheduleMapper, never()).updateBatch(any(), any(), any(), any());
    }

    @Test
    void update_nullTotalItems_shouldReject() {
        JobSchedule batch = new JobSchedule();
        batch.setId(5L);
        batch.setJobId(10L);
        when(jobScheduleMapper.findById(5L)).thenReturn(Optional.of(batch));
        when(jobMapper.findById(10L)).thenReturn(Optional.of(ownedAnnotationJob()));

        AnnotationBatchUpdateCmd cmd = new AnnotationBatchUpdateCmd();
        cmd.setId(5L);
        cmd.setBatchCode("CODE");
        cmd.setTotalItems(null);

        assertThatThrownBy(() -> service.update(1L, cmd))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("总数量不能为空");
        verify(jobScheduleMapper, never()).updateBatch(any(), any(), any(), any());
    }

    @Test
    void update_nonPositiveTotalItems_shouldReject() {
        JobSchedule batch = new JobSchedule();
        batch.setId(5L);
        batch.setJobId(10L);
        when(jobScheduleMapper.findById(5L)).thenReturn(Optional.of(batch));
        when(jobMapper.findById(10L)).thenReturn(Optional.of(ownedAnnotationJob()));

        AnnotationBatchUpdateCmd cmd = new AnnotationBatchUpdateCmd();
        cmd.setId(5L);
        cmd.setBatchCode("CODE");
        cmd.setTotalItems(-1);

        assertThatThrownBy(() -> service.update(1L, cmd))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("总数量必须大于0");
        verify(jobScheduleMapper, never()).updateBatch(any(), any(), any(), any());
    }

    @Test
    void update_scheduleDateNotNull_shouldReject() {
        JobSchedule batch = new JobSchedule();
        batch.setId(5L);
        batch.setJobId(10L);
        batch.setScheduleDate(java.time.LocalDate.now());
        when(jobScheduleMapper.findById(5L)).thenReturn(Optional.of(batch));

        AnnotationBatchUpdateCmd cmd = new AnnotationBatchUpdateCmd();
        cmd.setId(5L);
        cmd.setBatchCode("CODE");
        cmd.setTotalItems(100);

        assertThatThrownBy(() -> service.update(1L, cmd))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("不是标注批次");
        verify(jobScheduleMapper, never()).updateBatch(any(), any(), any(), any());
    }

    @Test
    void update_batchNotOwned_shouldReject() {
        JobSchedule batch = new JobSchedule();
        batch.setId(5L);
        batch.setJobId(10L);
        when(jobScheduleMapper.findById(5L)).thenReturn(Optional.of(batch));
        Job otherJob = ownedAnnotationJob();
        otherJob.setCompanyId(2L);
        when(jobMapper.findById(10L)).thenReturn(Optional.of(otherJob));

        AnnotationBatchUpdateCmd cmd = new AnnotationBatchUpdateCmd();
        cmd.setId(5L);
        cmd.setBatchCode("CODE");
        cmd.setTotalItems(100);

        assertThatThrownBy(() -> service.update(1L, cmd))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("标注批次不存在");
        verify(jobScheduleMapper, never()).updateBatch(any(), any(), any(), any());
    }

    @Test
    void toggle_active_shouldUpdateStatus() {
        JobSchedule batch = new JobSchedule();
        batch.setId(5L);
        batch.setJobId(10L);
        batch.setStatus("CANCELLED");
        when(jobScheduleMapper.findById(5L)).thenReturn(Optional.of(batch));
        when(jobMapper.findById(10L)).thenReturn(Optional.of(ownedAnnotationJob()));
        when(taskOrderMapper.aggregateProgress(anyList())).thenReturn(Collections.emptyList());

        AnnotationBatchToggleCmd cmd = new AnnotationBatchToggleCmd();
        cmd.setId(5L);
        cmd.setStatus("ACTIVE");

        AnnotationBatchVO vo = service.toggle(1L, cmd);

        verify(jobScheduleMapper).updateStatus(5L, "ACTIVE");
        assertThat(vo.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void toggle_cancelled_shouldUpdateStatus() {
        JobSchedule batch = new JobSchedule();
        batch.setId(5L);
        batch.setJobId(10L);
        batch.setStatus("ACTIVE");
        when(jobScheduleMapper.findById(5L)).thenReturn(Optional.of(batch));
        when(jobMapper.findById(10L)).thenReturn(Optional.of(ownedAnnotationJob()));
        when(taskOrderMapper.aggregateProgress(anyList())).thenReturn(Collections.emptyList());

        AnnotationBatchToggleCmd cmd = new AnnotationBatchToggleCmd();
        cmd.setId(5L);
        cmd.setStatus("CANCELLED");

        AnnotationBatchVO vo = service.toggle(1L, cmd);

        verify(jobScheduleMapper).updateStatus(5L, "CANCELLED");
        assertThat(vo.getStatus()).isEqualTo("CANCELLED");
    }

    @Test
    void toggle_otherStatus_shouldReject() {
        AnnotationBatchToggleCmd cmd = new AnnotationBatchToggleCmd();
        cmd.setId(5L);
        cmd.setStatus("PAUSED");

        assertThatThrownBy(() -> service.toggle(1L, cmd))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("状态仅支持");
        verify(jobScheduleMapper, never()).updateStatus(any(), any());
    }

    @Test
    void toggle_blankStatus_shouldReject() {
        AnnotationBatchToggleCmd cmd = new AnnotationBatchToggleCmd();
        cmd.setId(5L);
        cmd.setStatus("  ");

        assertThatThrownBy(() -> service.toggle(1L, cmd))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("状态仅支持");
        verify(jobScheduleMapper, never()).updateStatus(any(), any());
    }

    @Test
    void toggle_batchNotOwned_shouldReject() {
        JobSchedule batch = new JobSchedule();
        batch.setId(5L);
        batch.setJobId(10L);
        when(jobScheduleMapper.findById(5L)).thenReturn(Optional.of(batch));
        Job otherJob = ownedAnnotationJob();
        otherJob.setCompanyId(2L);
        when(jobMapper.findById(10L)).thenReturn(Optional.of(otherJob));

        AnnotationBatchToggleCmd cmd = new AnnotationBatchToggleCmd();
        cmd.setId(5L);
        cmd.setStatus("ACTIVE");

        assertThatThrownBy(() -> service.toggle(1L, cmd))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("标注批次不存在");
        verify(jobScheduleMapper, never()).updateStatus(any(), any());
    }

    @Test
    void toggle_nullId_shouldReject() {
        AnnotationBatchToggleCmd cmd = new AnnotationBatchToggleCmd();
        cmd.setId(null);
        cmd.setStatus("ACTIVE");

        assertThatThrownBy(() -> service.toggle(1L, cmd))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("批次ID不能为空");
    }

    @Test
    void list_shouldAssembleProgress() {
        when(jobMapper.findById(10L)).thenReturn(Optional.of(ownedAnnotationJob()));

        JobSchedule batch = new JobSchedule();
        batch.setId(5L);
        batch.setJobId(10L);
        batch.setBatchCode("B1");
        batch.setTotalItems(1000);
        batch.setStatus("ACTIVE");
        when(jobScheduleMapper.findBatchesByJobId(10L)).thenReturn(List.of(batch));

        AnnotationBatchProgress progress = new AnnotationBatchProgress();
        progress.setScheduleId(5L);
        progress.setGrabbedCount(800);
        progress.setCompletedCount(500);
        when(taskOrderMapper.aggregateProgress(List.of(5L))).thenReturn(List.of(progress));

        AnnotationBatchListCmd cmd = new AnnotationBatchListCmd();
        cmd.setJobId(10L);

        List<AnnotationBatchVO> result = service.list(1L, cmd);

        assertThat(result).hasSize(1);
        AnnotationBatchVO vo = result.get(0);
        assertThat(vo.getBatchCode()).isEqualTo("B1");
        assertThat(vo.getTotalItems()).isEqualTo(1000);
        assertThat(vo.getGrabbedCount()).isEqualTo(800);
        assertThat(vo.getCompletedCount()).isEqualTo(500);
        assertThat(vo.getProgress()).isEqualTo(50);
    }

    @Test
    void list_totalItemsZero_shouldProgressZero() {
        when(jobMapper.findById(10L)).thenReturn(Optional.of(ownedAnnotationJob()));

        JobSchedule batch = new JobSchedule();
        batch.setId(5L);
        batch.setJobId(10L);
        batch.setBatchCode("B1");
        batch.setTotalItems(0);
        batch.setStatus("ACTIVE");
        when(jobScheduleMapper.findBatchesByJobId(10L)).thenReturn(List.of(batch));

        AnnotationBatchProgress progress = new AnnotationBatchProgress();
        progress.setScheduleId(5L);
        progress.setGrabbedCount(100);
        progress.setCompletedCount(50);
        when(taskOrderMapper.aggregateProgress(List.of(5L))).thenReturn(List.of(progress));

        AnnotationBatchListCmd cmd = new AnnotationBatchListCmd();
        cmd.setJobId(10L);

        List<AnnotationBatchVO> result = service.list(1L, cmd);

        assertThat(result.get(0).getProgress()).isEqualTo(0);
        assertThat(result.get(0).getGrabbedCount()).isEqualTo(100);
        assertThat(result.get(0).getCompletedCount()).isEqualTo(50);
    }

    @Test
    void list_nullProgress_shouldDefaultToZero() {
        when(jobMapper.findById(10L)).thenReturn(Optional.of(ownedAnnotationJob()));

        JobSchedule batch = new JobSchedule();
        batch.setId(5L);
        batch.setJobId(10L);
        batch.setBatchCode("B1");
        batch.setTotalItems(1000);
        batch.setStatus("ACTIVE");
        when(jobScheduleMapper.findBatchesByJobId(10L)).thenReturn(List.of(batch));
        when(taskOrderMapper.aggregateProgress(List.of(5L))).thenReturn(Collections.emptyList());

        AnnotationBatchListCmd cmd = new AnnotationBatchListCmd();
        cmd.setJobId(10L);

        List<AnnotationBatchVO> result = service.list(1L, cmd);

        assertThat(result.get(0).getGrabbedCount()).isEqualTo(0);
        assertThat(result.get(0).getCompletedCount()).isEqualTo(0);
        assertThat(result.get(0).getProgress()).isEqualTo(0);
    }

    @Test
    void list_nullJobId_shouldReject() {
        AnnotationBatchListCmd cmd = new AnnotationBatchListCmd();
        cmd.setJobId(null);

        assertThatThrownBy(() -> service.list(1L, cmd))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("岗位ID不能为空");
    }

    @Test
    void list_jobNotOwned_shouldReject() {
        Job job = ownedAnnotationJob();
        job.setCompanyId(2L);
        when(jobMapper.findById(10L)).thenReturn(Optional.of(job));

        AnnotationBatchListCmd cmd = new AnnotationBatchListCmd();
        cmd.setJobId(10L);

        assertThatThrownBy(() -> service.list(1L, cmd))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("岗位不存在");
    }
}
