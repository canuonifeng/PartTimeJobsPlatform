package com.parttime.enterprise.service;

import com.parttime.enterprise.pojo.entity.AnnotationTaskOrder;
import com.parttime.enterprise.pojo.vo.PageVO;

import java.util.List;

public interface TaskOrderService {

    AnnotationTaskOrder createByApplication(Long applicationId);

    PageVO<AnnotationTaskOrder> getTaskOrders(Long companyId, Long jobId, Long workerId, String status, Integer page, Integer pageSize);

    List<AnnotationTaskOrder> getTaskOrdersByWorker(Long workerId);

    AnnotationTaskOrder getTaskOrderById(Long id);
}
