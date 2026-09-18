package com.parttime.enterprise.service;

import com.parttime.enterprise.pojo.entity.AnnotationTaskOrder;
import com.parttime.enterprise.pojo.entity.Job;

public interface AnnotationSettlementService {
    void settleAnnotationTask(AnnotationTaskOrder taskOrder, Job job);
}
