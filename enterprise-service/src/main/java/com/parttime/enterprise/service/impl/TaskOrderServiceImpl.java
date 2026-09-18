package com.parttime.enterprise.service.impl;

import com.parttime.enterprise.enums.AnnotationTaskOrderStatus;
import com.parttime.enterprise.enums.PricingMode;
import com.parttime.enterprise.enums.TaskType;
import com.parttime.enterprise.mapper.AnnotationTaskOrderMapper;
import com.parttime.enterprise.mapper.JobMapper;
import com.parttime.enterprise.mapper.JobScheduleMapper;
import com.parttime.enterprise.mapper.ScheduleApplicationMapper;
import com.parttime.enterprise.pojo.entity.AnnotationTaskOrder;
import com.parttime.enterprise.pojo.entity.Job;
import com.parttime.enterprise.pojo.entity.JobSchedule;
import com.parttime.enterprise.pojo.entity.ScheduleApplication;
import com.parttime.enterprise.pojo.vo.PageVO;
import com.parttime.enterprise.service.TaskOrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

@Service
public class TaskOrderServiceImpl implements TaskOrderService {

    @Resource
    private AnnotationTaskOrderMapper taskOrderMapper;
    @Resource
    private ScheduleApplicationMapper applicationMapper;
    @Resource
    private JobScheduleMapper jobScheduleMapper;
    @Resource
    private JobMapper jobMapper;

    @Override
    @Transactional
    public AnnotationTaskOrder createByApplication(Long applicationId) {
        ScheduleApplication app = applicationMapper.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found: " + applicationId));

        JobSchedule schedule = jobScheduleMapper.findById(app.getScheduleId())
                .orElseThrow(() -> new RuntimeException("Schedule not found: " + app.getScheduleId()));

        Job job = jobMapper.findById(schedule.getJobId())
                .orElseThrow(() -> new RuntimeException("Job not found: " + schedule.getJobId()));

        if (!TaskType.ANNOTATION.getCode().equals(job.getTaskType())) {
            throw new RuntimeException("Job is not ANNOTATION type: " + job.getId());
        }

        AnnotationTaskOrder existing = taskOrderMapper.selectByApplicationId(applicationId);
        if (existing != null) {
            return existing;
        }

        AnnotationTaskOrder order = new AnnotationTaskOrder();
        order.setApplicationId(applicationId);
        order.setJobScheduleId(schedule.getId());
        order.setWorkerId(app.getWorkerId());
        order.setJobId(job.getId());
        order.setStatus(AnnotationTaskOrderStatus.PENDING.getCode());
        order.setCompletedItems(0);
        order.setTotalItems(job.getTotalItems());
        order.setPricingMode(job.getPricingMode());
        order.setUnitPrice(job.getPricePerUnit());
        order.setTotalAmount(calculateTotalAmount(job));
        taskOrderMapper.insert(order);
        return taskOrderMapper.selectById(order.getId());
    }

    @Override
    public PageVO<AnnotationTaskOrder> getTaskOrders(Long companyId, Long jobId, Long workerId, String status, Integer page, Integer pageSize) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safePageSize = pageSize == null || pageSize < 1 ? 20 : Math.min(pageSize, 100);
        int offset = (safePage - 1) * safePageSize;
        String normalizedStatus = status == null || status.isBlank() ? null : status.trim().toUpperCase();
        List<AnnotationTaskOrder> orders = taskOrderMapper.findPage(companyId, workerId, jobId, normalizedStatus, offset, safePageSize);
        long total = taskOrderMapper.countPage(companyId, workerId, jobId, normalizedStatus);
        return new PageVO<>(orders, total);
    }

    @Override
    public List<AnnotationTaskOrder> getTaskOrdersByWorker(Long workerId) {
        return taskOrderMapper.selectByWorkerId(workerId);
    }

    @Override
    public AnnotationTaskOrder getTaskOrderById(Long id) {
        return taskOrderMapper.selectById(id);
    }

    private BigDecimal calculateTotalAmount(Job job) {
        if (job.getPricingMode() == null || job.getPricePerUnit() == null || job.getTotalItems() == null) {
            return null;
        }
        return switch (PricingMode.valueOf(job.getPricingMode())) {
            case PER_ITEM -> job.getPricePerUnit().multiply(BigDecimal.valueOf(job.getTotalItems()));
            case PER_PACKAGE -> job.getPricePerUnit();
        };
    }
}
