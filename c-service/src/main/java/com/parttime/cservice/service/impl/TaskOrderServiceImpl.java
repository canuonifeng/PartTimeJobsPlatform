package com.parttime.cservice.service.impl;

import com.parttime.cservice.mapper.AnnotationTaskOrderMapper;
import com.parttime.cservice.mapper.CompanyWorkerInsertMapper;
import com.parttime.cservice.mapper.JobMapper;
import com.parttime.cservice.mapper.JobScheduleMapper;
import com.parttime.cservice.mapper.ScheduleApplicationMapper;
import com.parttime.cservice.pojo.cmd.GrabTaskOrderCmd;
import com.parttime.cservice.pojo.entity.AnnotationTaskOrder;
import com.parttime.cservice.pojo.entity.Job;
import com.parttime.cservice.pojo.entity.JobSchedule;
import com.parttime.cservice.pojo.entity.ScheduleApplication;
import com.parttime.cservice.pojo.vo.PageVO;
import com.parttime.cservice.pojo.vo.TaskOrderVO;
import com.parttime.cservice.service.CertificationGateService;
import com.parttime.cservice.service.TaskOrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TaskOrderServiceImpl implements TaskOrderService {

    @Resource
    private AnnotationTaskOrderMapper annotationTaskOrderMapper;

    @Resource
    private JobMapper jobMapper;

    @Resource
    private JobScheduleMapper jobScheduleMapper;

    @Resource
    private ScheduleApplicationMapper scheduleApplicationMapper;

    @Resource
    private CompanyWorkerInsertMapper companyWorkerInsertMapper;

    @Resource
    private CertificationGateService certificationGateService;

    @Override
    public PageVO<TaskOrderVO> getMyTaskOrders(Long workerId, int page, int pageSize) {
        List<AnnotationTaskOrder> allOrders = annotationTaskOrderMapper.selectByWorkerId(workerId);

        List<Long> jobIds = allOrders.stream()
                .map(AnnotationTaskOrder::getJobId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Job> jobMap = jobIds.isEmpty()
                ? Map.of()
                : jobMapper.findByJobIds(jobIds).stream()
                        .collect(Collectors.toMap(Job::getId, j -> j));

        List<TaskOrderVO> voList = allOrders.stream()
                .map(order -> {
                    TaskOrderVO vo = new TaskOrderVO();
                    vo.setId(order.getId());
                    vo.setJobId(order.getJobId());
                    vo.setScheduleId(order.getScheduleId());
                    vo.setItemsCompleted(order.getItemsCompleted());
                    vo.setStatus(order.getStatus());
                    vo.setSubmittedAt(order.getSubmittedAt());
                    vo.setCompletedAt(order.getCompletedAt());
                    vo.setCreatedAt(order.getCreatedAt());

                    Job job = jobMap.get(order.getJobId());
                    if (job != null) {
                        vo.setJobTitle(job.getTitle());
                        vo.setTotalItems(job.getTotalItems());
                        vo.setPricingMode(job.getPricingMode());
                        vo.setUnitPrice(job.getPricePerUnit());
                        if (job.getPricePerUnit() != null && order.getItemsCompleted() != null) {
                            vo.setTotalAmount(job.getPricePerUnit().multiply(BigDecimal.valueOf(order.getItemsCompleted())));
                        }
                    }
                    return vo;
                })
                .collect(Collectors.toList());

        int total = voList.size();
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<TaskOrderVO> paged = fromIndex < total ? voList.subList(fromIndex, toIndex) : List.of();

        return new PageVO<>(paged, total);
    }

    @Override
    @Transactional
    public int grabTaskOrder(Long workerId, GrabTaskOrderCmd cmd) {
        Long jobId = cmd.getJobId();
        if (jobId == null) {
            throw new RuntimeException("任务ID不能为空");
        }
        Job job = jobMapper.findByJobId(jobId).orElse(null);
        if (job == null) {
            throw new RuntimeException("任务不存在");
        }
        if (!"ANNOTATION".equals(job.getTaskType())) {
            throw new RuntimeException("该任务不支持抢单");
        }
        if (job.getDeadline() != null && LocalDateTime.now().isAfter(job.getDeadline())) {
            throw new RuntimeException("报名已截止");
        }
        // 认证校验：标注任务必须持证才能抢单
        certificationGateService.checkCertification(workerId, job.getTaskType());

        List<Long> targetScheduleIds = cmd.getScheduleIds();
        List<JobSchedule> batches;
        if (targetScheduleIds != null && !targetScheduleIds.isEmpty()) {
            batches = jobScheduleMapper.findByIds(targetScheduleIds);
        } else {
            batches = jobScheduleMapper.findActiveByJobId(jobId);
        }
        if (batches.isEmpty()) {
            throw new RuntimeException("暂无可用批次");
        }
        LocalDateTime now = LocalDateTime.now();
        List<Long> alreadyApplied = scheduleApplicationMapper.findScheduleIdsByWorkerIdAndJobId(workerId, jobId);
        List<JobSchedule> grabbable = batches.stream()
                .filter(b -> "ACTIVE".equals(b.getStatus()))
                .filter(b -> {
                    if (b.getScheduleDate() == null || b.getStartTime() == null) {
                        return true;
                    }
                    return LocalDateTime.of(b.getScheduleDate(), b.getStartTime()).isAfter(now);
                })
                .filter(b -> !alreadyApplied.contains(b.getId()))
                .toList();
        if (grabbable.isEmpty()) {
            throw new RuntimeException("所选批次已全部抢过或不可用");
        }
        Map<Long, Integer> acceptedCounts = scheduleApplicationMapper.countAcceptedByScheduleIds(
                grabbable.stream().map(JobSchedule::getId).toList());
        List<JobSchedule> available = grabbable.stream()
                .filter(b -> {
                    Integer capacity = b.getSlotsAvailable();
                    return capacity == null || capacity <= 0 || acceptedCounts.getOrDefault(b.getId(), 0) < capacity;
                })
                .toList();
        if (available.isEmpty()) {
            throw new RuntimeException("批次已满，无法抢单");
        }
        List<ScheduleApplication> applications = available.stream().map(b -> {
            ScheduleApplication sa = new ScheduleApplication();
            sa.setScheduleId(b.getId());
            sa.setWorkerId(workerId);
            sa.setStatus("PENDING");
            return sa;
        }).collect(Collectors.toList());
        scheduleApplicationMapper.batchInsert(applications);
        if (job.getCompanyId() != null) {
            companyWorkerInsertMapper.upsert(job.getCompanyId(), workerId);
        }
        return applications.size();
    }
}
