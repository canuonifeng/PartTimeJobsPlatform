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
    public List<AnnotationBatchVO> list(Long companyId, AnnotationBatchListCmd cmd) {
        if (cmd.getJobId() == null) {
            throw new BusinessException("岗位ID不能为空");
        }
        Job job = requireOwnedJob(companyId, cmd.getJobId());
        List<JobSchedule> batches = jobScheduleMapper.findBatchesByJobId(job.getId());
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
    public AnnotationBatchVO create(Long companyId, AnnotationBatchCreateCmd cmd) {
        if (cmd.getJobId() == null) {
            throw new BusinessException("岗位ID不能为空");
        }
        Job job = requireOwnedJob(companyId, cmd.getJobId());
        if (!TaskType.ANNOTATION.getCode().equals(job.getTaskType())) {
            throw new BusinessException("该岗位不是标注类型职位");
        }
        validateBatchFields(cmd.getBatchCode(), cmd.getTotalItems());

        JobSchedule batch = new JobSchedule();
        batch.setJobId(job.getId());
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
    public AnnotationBatchVO update(Long companyId, AnnotationBatchUpdateCmd cmd) {
        JobSchedule batch = requireOwnedBatch(companyId, cmd.getId());
        validateBatchFields(cmd.getBatchCode(), cmd.getTotalItems());
        jobScheduleMapper.updateBatch(cmd.getId(), cmd.getBatchCode().trim(), cmd.getTotalItems(), cmd.getExternalBatchId());
        batch.setBatchCode(cmd.getBatchCode().trim());
        batch.setTotalItems(cmd.getTotalItems());
        batch.setExternalBatchId(cmd.getExternalBatchId());
        return toVO(batch, progressOf(cmd.getId()));
    }

    @Override
    @Transactional
    public AnnotationBatchVO toggle(Long companyId, AnnotationBatchToggleCmd cmd) {
        if (cmd.getId() == null) {
            throw new BusinessException("批次ID不能为空");
        }
        String normalizedStatus = cmd.getStatus() == null ? "" : cmd.getStatus().trim();
        if (!"ACTIVE".equals(normalizedStatus) && !"CANCELLED".equals(normalizedStatus)) {
            throw new BusinessException("状态仅支持 ACTIVE/CANCELLED");
        }
        JobSchedule batch = requireOwnedBatch(companyId, cmd.getId());
        jobScheduleMapper.updateStatus(cmd.getId(), normalizedStatus);
        batch.setStatus(normalizedStatus);
        return toVO(batch, progressOf(cmd.getId()));
    }

    private Job requireOwnedJob(Long companyId, Long jobId) {
        Job job = jobMapper.findById(jobId)
                .orElseThrow(() -> new BusinessException("岗位不存在"));
        if (!companyId.equals(job.getCompanyId())) {
            throw new BusinessException("岗位不存在");
        }
        return job;
    }

    private JobSchedule requireOwnedBatch(Long companyId, Long id) {
        if (id == null) {
            throw new BusinessException("批次ID不能为空");
        }
        JobSchedule batch = jobScheduleMapper.findById(id)
                .orElseThrow(() -> new BusinessException("标注批次不存在"));
        if (batch.getScheduleDate() != null) {
            throw new BusinessException("该记录不是标注批次");
        }
        Job job = jobMapper.findById(batch.getJobId())
                .orElseThrow(() -> new BusinessException("标注批次不存在"));
        if (!companyId.equals(job.getCompanyId())) {
            throw new BusinessException("标注批次不存在");
        }
        return batch;
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
