package com.parttime.enterprise.service.impl;

import com.parttime.enterprise.enums.AnnotationTaskOrderStatus;
import com.parttime.enterprise.exception.BusinessException;
import com.parttime.enterprise.mapper.AnnotationTaskOrderMapper;
import com.parttime.enterprise.mapper.ExternalWorkerMappingMapper;
import com.parttime.enterprise.mapper.JobMapper;
import com.parttime.enterprise.mapper.JobScheduleMapper;
import com.parttime.enterprise.mapper.ScheduleApplicationMapper;
import com.parttime.enterprise.pojo.cmd.AnnotationSubmitCmd;
import com.parttime.enterprise.pojo.cmd.ProgressCmd;
import com.parttime.enterprise.pojo.cmd.QualityCheckCmd;
import com.parttime.enterprise.pojo.entity.AnnotationTaskOrder;
import com.parttime.enterprise.pojo.entity.ExternalWorkerMapping;
import com.parttime.enterprise.pojo.entity.Job;
import com.parttime.enterprise.pojo.entity.JobSchedule;
import com.parttime.enterprise.pojo.entity.ScheduleApplication;
import com.parttime.enterprise.service.ExternalCallbackService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ExternalCallbackServiceImpl implements ExternalCallbackService {

    @Value("${external.callback.api-key:}")
    private String callbackApiKey;

    @Resource
    private JobMapper jobMapper;
    @Resource
    private JobScheduleMapper jobScheduleMapper;
    @Resource
    private ExternalWorkerMappingMapper externalWorkerMappingMapper;
    @Resource
    private AnnotationTaskOrderMapper taskOrderMapper;
    @Resource
    private ScheduleApplicationMapper applicationMapper;

    @Override
    @Transactional
    public void handleAnnotationSubmit(AnnotationSubmitCmd cmd, String callbackKey) {
        verifyApiKey(callbackKey);

        Job job = jobMapper.findByExternalTaskId(cmd.getExternalTaskId());
        if (job == null) {
            throw new BusinessException("未找到对应外部任务ID的岗位: " + cmd.getExternalTaskId());
        }

        JobSchedule schedule = jobScheduleMapper.findByExternalBatchId(cmd.getExternalBatchId());
        if (schedule == null) {
            throw new BusinessException("未找到对应外部批次ID的排班: " + cmd.getExternalBatchId());
        }

        ExternalWorkerMapping mapping = externalWorkerMappingMapper.selectByExternalId(null, cmd.getExternalWorkerId());
        if (mapping == null) {
            throw new BusinessException("未找到对应外部工人ID的映射: " + cmd.getExternalWorkerId());
        }
        Long workerId = mapping.getWorkerId();

        AnnotationTaskOrder existing = taskOrderMapper.selectByJobIdAndScheduleIdAndWorkerId(
                job.getId(), schedule.getId(), workerId);
        if (existing != null) {
            if (existing.getExternalSubmissionId() != null
                    && existing.getExternalSubmissionId().equals(cmd.getExternalSubmissionId())) {
                return;
            }
            existing.setStatus(AnnotationTaskOrderStatus.SUBMITTED.getCode());
            existing.setCompletedItems(cmd.getItemsCompleted());
            existing.setExternalSubmissionId(cmd.getExternalSubmissionId());
            existing.setSubmittedAt(LocalDateTime.now());
            taskOrderMapper.update(existing);
            return;
        }

        ScheduleApplication application = applicationMapper.findByScheduleIdAndWorkerId(schedule.getId(), workerId);
        if (application == null) {
            throw new BusinessException("未找到对应排班和工人的报名记录");
        }

        AnnotationTaskOrder order = taskOrderMapper.selectByApplicationId(application.getId());
        if (order == null) {
            order = new AnnotationTaskOrder();
            order.setApplicationId(application.getId());
            order.setJobScheduleId(schedule.getId());
            order.setWorkerId(workerId);
            order.setJobId(job.getId());
            order.setStatus(AnnotationTaskOrderStatus.SUBMITTED.getCode());
            order.setCompletedItems(cmd.getItemsCompleted());
            order.setExternalSubmissionId(cmd.getExternalSubmissionId());
            order.setSubmittedAt(LocalDateTime.now());
            taskOrderMapper.insert(order);
        } else {
            if (order.getExternalSubmissionId() != null
                    && order.getExternalSubmissionId().equals(cmd.getExternalSubmissionId())) {
                return;
            }
            order.setStatus(AnnotationTaskOrderStatus.SUBMITTED.getCode());
            order.setCompletedItems(cmd.getItemsCompleted());
            order.setExternalSubmissionId(cmd.getExternalSubmissionId());
            order.setSubmittedAt(LocalDateTime.now());
            taskOrderMapper.update(order);
        }
    }

    @Override
    @Transactional
    public void handleQualityCheck(QualityCheckCmd cmd, String callbackKey) {
        verifyApiKey(callbackKey);

        Job job = jobMapper.findByExternalTaskId(cmd.getExternalTaskId());
        if (job == null) {
            throw new BusinessException("未找到对应外部任务ID的岗位: " + cmd.getExternalTaskId());
        }

        JobSchedule schedule = jobScheduleMapper.findByExternalBatchId(cmd.getExternalBatchId());
        if (schedule == null) {
            throw new BusinessException("未找到对应外部批次ID的排班: " + cmd.getExternalBatchId());
        }

        ExternalWorkerMapping mapping = externalWorkerMappingMapper.selectByExternalId(null, cmd.getExternalWorkerId());
        if (mapping == null) {
            throw new BusinessException("未找到对应外部工人ID的映射: " + cmd.getExternalWorkerId());
        }
        Long workerId = mapping.getWorkerId();

        AnnotationTaskOrder order = taskOrderMapper.selectByJobIdAndScheduleIdAndWorkerId(
                job.getId(), schedule.getId(), workerId);
        if (order == null) {
            throw new BusinessException("未找到对应的任务订单");
        }

        if (Boolean.TRUE.equals(cmd.getPassed())) {
            order.setStatus(AnnotationTaskOrderStatus.COMPLETED.getCode());
            order.setCompletedAt(LocalDateTime.now());
        } else {
            order.setStatus(AnnotationTaskOrderStatus.REJECTED.getCode());
        }
        if (cmd.getItemsCompleted() != null) {
            order.setCompletedItems(cmd.getItemsCompleted());
        }
        taskOrderMapper.update(order);
    }

    @Override
    @Transactional
    public void handleProgress(ProgressCmd cmd, String callbackKey) {
        verifyApiKey(callbackKey);

        Job job = jobMapper.findByExternalTaskId(cmd.getExternalTaskId());
        if (job == null) {
            throw new BusinessException("未找到对应外部任务ID的岗位: " + cmd.getExternalTaskId());
        }

        JobSchedule schedule = jobScheduleMapper.findByExternalBatchId(cmd.getExternalBatchId());
        if (schedule == null) {
            throw new BusinessException("未找到对应外部批次ID的排班: " + cmd.getExternalBatchId());
        }

        ExternalWorkerMapping mapping = externalWorkerMappingMapper.selectByExternalId(null, cmd.getExternalWorkerId());
        if (mapping == null) {
            throw new BusinessException("未找到对应外部工人ID的映射: " + cmd.getExternalWorkerId());
        }
        Long workerId = mapping.getWorkerId();

        AnnotationTaskOrder order = taskOrderMapper.selectByJobIdAndScheduleIdAndWorkerId(
                job.getId(), schedule.getId(), workerId);
        if (order == null) {
            throw new BusinessException("未找到对应的任务订单");
        }

        order.setCompletedItems(cmd.getItemsCompleted());
        if (AnnotationTaskOrderStatus.PENDING.getCode().equals(order.getStatus())) {
            order.setStatus(AnnotationTaskOrderStatus.IN_PROGRESS.getCode());
        }
        taskOrderMapper.update(order);
    }

    private void verifyApiKey(String callbackKey) {
        if (callbackApiKey == null || callbackApiKey.isBlank()) {
            return;
        }
        if (!callbackApiKey.equals(callbackKey)) {
            throw new BusinessException("无效的回调密钥");
        }
    }
}
