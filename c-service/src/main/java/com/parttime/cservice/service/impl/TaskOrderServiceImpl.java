package com.parttime.cservice.service.impl;

import com.parttime.cservice.mapper.AnnotationTaskOrderMapper;
import com.parttime.cservice.mapper.JobMapper;
import com.parttime.cservice.pojo.entity.AnnotationTaskOrder;
import com.parttime.cservice.pojo.entity.Job;
import com.parttime.cservice.pojo.vo.PageVO;
import com.parttime.cservice.pojo.vo.TaskOrderVO;
import com.parttime.cservice.service.TaskOrderService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TaskOrderServiceImpl implements TaskOrderService {

    @Resource
    private AnnotationTaskOrderMapper annotationTaskOrderMapper;

    @Resource
    private JobMapper jobMapper;

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
}
