package com.parttime.enterprise.service.impl;

import com.parttime.enterprise.mapper.BalanceTransactionMapper;
import com.parttime.enterprise.mapper.WorkerBalanceMapper;
import com.parttime.enterprise.mapper.WorkerNotificationMapper;
import com.parttime.enterprise.pojo.entity.AnnotationTaskOrder;
import com.parttime.enterprise.pojo.entity.BalanceTransaction;
import com.parttime.enterprise.pojo.entity.Job;
import com.parttime.enterprise.pojo.entity.WorkerBalance;
import com.parttime.enterprise.service.AnnotationSettlementService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class AnnotationSettlementServiceImpl implements AnnotationSettlementService {

    @Resource
    private WorkerBalanceMapper workerBalanceMapper;

    @Resource
    private BalanceTransactionMapper balanceTransactionMapper;

    @Resource
    private WorkerNotificationMapper workerNotificationMapper;

    @Override
    @Transactional
    public void settleAnnotationTask(AnnotationTaskOrder taskOrder, Job job) {
        // 计价信息以 Job 为准（annotation_task_orders 表不落库计价字段）
        String pricingMode = job.getPricingMode();
        BigDecimal unitPrice = job.getPricePerUnit();
        if (unitPrice == null) {
            throw new IllegalStateException("标注任务缺少单价，无法结算: jobId=" + job.getId());
        }
        BigDecimal amount;
        if ("PER_ITEM".equals(pricingMode)) {
            int completed = taskOrder.getCompletedItems() == null ? 0 : taskOrder.getCompletedItems();
            amount = BigDecimal.valueOf(completed).multiply(unitPrice);
        } else {
            amount = unitPrice;
        }

        Long workerId = taskOrder.getWorkerId();

        WorkerBalance wb = workerBalanceMapper.findByWorkerId(workerId);
        if (wb == null) {
            workerBalanceMapper.upsert(workerId, amount, amount, BigDecimal.ZERO);
        } else {
            workerBalanceMapper.upsert(workerId,
                    wb.getBalance().add(amount),
                    wb.getTotalEarned().add(amount),
                    wb.getTotalWithdrawn());
        }

        BalanceTransaction bt = new BalanceTransaction();
        bt.setWorkerId(workerId);
        bt.setAmount(amount);
        bt.setType("EARNINGS");
        bt.setDescription("标注任务结算: " + job.getTitle());
        balanceTransactionMapper.insert(bt);

        taskOrder.setSettledAt(LocalDateTime.now());
        taskOrder.setStatus("COMPLETED");

        workerNotificationMapper.insertWorkerNotification(workerId, "EARNINGS_SETTLED", "finance",
                "收入到账", "您有一笔标注任务收入" + amount + "元已到账", "ANNOTATION_TASK", taskOrder.getId());
    }
}
