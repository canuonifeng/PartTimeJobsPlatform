package com.parttime.enterprise.service.impl;

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
import com.parttime.enterprise.service.AnnotationBatchService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AnnotationBatchServiceImpl implements AnnotationBatchService {

    @Resource
    private JobScheduleMapper jobScheduleMapper;
    @Resource
    private AnnotationTaskOrderMapper taskOrderMapper;
    @Resource
    private JobMapper jobMapper;

    @Override
    public List<AnnotationBatchVO> list(AnnotationBatchListCmd cmd) {
        if (cmd.getJobId() == null) {
            throw new BusinessException("岗位ID不能为空");
        }
        List<JobSchedule> batches = jobScheduleMapper.findBatchesByJobId(cmd.getJobId());
        List<Long> ids = batches.stream().map(JobSchedule::getId).collect(Collectors.toList());
        Map<Long, AnnotationBatchProgress> progressMap = ids.isEmpty()
                ? Collections.emptyMap()
                : taskOrderMapper.aggregateProgress(ids).stream()
                    .collect(Collectors.toMap(AnnotationBatchProgress::getScheduleId, Function.identity()));
        return batches.stream()
                .map(batch -> toVO(batch, progressMap.get(batch.getId())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AnnotationBatchVO create(AnnotationBatchCreateCmd cmd) {
        if (cmd.getJobId() == null) {
            throw new BusinessException("岗位ID不能为空");
        }
        Job job = jobMapper.findById(cmd.getJobId())
                .orElseThrow(() -> new BusinessException("岗位不存在"));
        if (!TaskType.ANNOTATION.getCode().equals(job.getTaskType())) {
            throw new BusinessException("该岗位不是标注类型职位");
        }
        validateBatchFields(cmd.getBatchCode(), cmd.getTotalItems());

        JobSchedule batch = new JobSchedule();
        batch.setJobId(cmd.getJobId());
        batch.setBatchCode(cmd.getBatchCode().trim());
        batch.setTotalItems(cmd.getTotalItems());
        batch.setExternalBatchId(cmd.getExternalBatchId());
        batch.setStatus("ACTIVE");
        batch.setSlotsAvailable(1);
        jobScheduleMapper.insert(batch);
        return toVO(batch, null);
    }

    @Override
    @Transactional
    public AnnotationBatchVO update(AnnotationBatchUpdateCmd cmd) {
        JobSchedule batch = requireBatch(cmd.getId());
        validateBatchFields(cmd.getBatchCode(), cmd.getTotalItems());
        jobScheduleMapper.updateBatch(cmd.getId(), cmd.getBatchCode().trim(), cmd.getTotalItems(), cmd.getExternalBatchId());
        batch.setBatchCode(cmd.getBatchCode().trim());
        batch.setTotalItems(cmd.getTotalItems());
        batch.setExternalBatchId(cmd.getExternalBatchId());
        return toVO(batch, progressOf(cmd.getId()));
    }

    @Override
    @Transactional
    public AnnotationBatchVO toggle(AnnotationBatchToggleCmd cmd) {
        if (cmd.getId() == null) {
            throw new BusinessException("批次ID不能为空");
        }
        if (cmd.getStatus() == null || cmd.getStatus().isBlank()) {
            throw new BusinessException("状态不能为空");
        }
        JobSchedule batch = requireBatch(cmd.getId());
        jobScheduleMapper.updateStatus(cmd.getId(), cmd.getStatus().trim());
        batch.setStatus(cmd.getStatus().trim());
        return toVO(batch, progressOf(cmd.getId()));
    }

    private JobSchedule requireBatch(Long id) {
        if (id == null) {
            throw new BusinessException("批次ID不能为空");
        }
        return jobScheduleMapper.findById(id)
                .orElseThrow(() -> new BusinessException("标注批次不存在"));
    }

    private void validateBatchFields(String batchCode, Integer totalItems) {
        if (batchCode == null || batchCode.isBlank()) {
            throw new BusinessException("批次编码不能为空");
        }
        if (totalItems == null) {
            throw new BusinessException("总数量不能为空");
        }
        if (totalItems <= 0) {
            throw new BusinessException("总数量必须大于0");
        }
    }

    private AnnotationBatchProgress progressOf(Long scheduleId) {
        List<AnnotationBatchProgress> list = taskOrderMapper.aggregateProgress(Collections.singletonList(scheduleId));
        return list.isEmpty() ? null : list.get(0);
    }

    private AnnotationBatchVO toVO(JobSchedule batch, AnnotationBatchProgress progress) {
        AnnotationBatchVO vo = new AnnotationBatchVO();
        vo.setId(batch.getId());
        vo.setJobId(batch.getJobId());
        vo.setBatchCode(batch.getBatchCode());
        vo.setTotalItems(batch.getTotalItems());
        vo.setExternalBatchId(batch.getExternalBatchId());
        vo.setStatus(batch.getStatus());
        int grabbed = progress == null || progress.getGrabbedCount() == null ? 0 : progress.getGrabbedCount();
        int completed = progress == null || progress.getCompletedCount() == null ? 0 : progress.getCompletedCount();
        vo.setGrabbedCount(grabbed);
        vo.setCompletedCount(completed);
        int total = batch.getTotalItems() == null ? 0 : batch.getTotalItems();
        int progressPercent = total > 0 ? (int) Math.round(completed * 100.0 / total) : 0;
        vo.setProgress(Math.min(100, Math.max(0, progressPercent)));
        return vo;
    }
}
