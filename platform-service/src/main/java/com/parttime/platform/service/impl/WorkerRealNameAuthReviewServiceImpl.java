package com.parttime.platform.service.impl;

import com.parttime.platform.mapper.WorkerMapper;
import com.parttime.platform.mapper.WorkerNotificationMapper;
import com.parttime.platform.mapper.WorkerRealNameAuthMapper;
import com.parttime.platform.pojo.entity.Worker;
import com.parttime.platform.pojo.entity.WorkerRealNameAuth;
import com.parttime.platform.pojo.vo.PageVO;
import com.parttime.platform.pojo.vo.WorkerRealNameAuthVO;
import com.parttime.platform.service.WorkerRealNameAuthReviewService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WorkerRealNameAuthReviewServiceImpl implements WorkerRealNameAuthReviewService {

    @Resource
    private WorkerRealNameAuthMapper mapper;

    @Resource
    private WorkerNotificationMapper workerNotificationMapper;

    @Resource
    private WorkerMapper workerMapper;

    @Override
    public PageVO<WorkerRealNameAuthVO> list(String status, int page, int pageSize) {
        int offset = Math.max(page - 1, 0) * pageSize;
        List<WorkerRealNameAuth> records = mapper.findPage(status, offset, pageSize);
        Map<Long, Worker> workerMap = loadWorkerMap(records);
        List<WorkerRealNameAuthVO> vos = records.stream()
                .map(r -> {
                    WorkerRealNameAuthVO vo = toVO(r);
                    Worker worker = workerMap.get(r.getWorkerId());
                    if (worker != null) {
                        vo.setWorkerName(worker.getName());
                        vo.setWorkerPhone(worker.getPhone());
                    }
                    return vo;
                })
                .collect(Collectors.toList());
        long total = mapper.countPage(status);
        return new PageVO<>(vos, total);
    }

    @Override
    public void approve(Long id, Long reviewerId) {
        WorkerRealNameAuth auth = mapper.findById(id)
                .orElseThrow(() -> new RuntimeException("记录不存在"));
        if (!"PENDING".equals(auth.getStatus())) {
            throw new RuntimeException("状态不允许操作");
        }
        mapper.updateReview(id, "APPROVED", null, reviewerId, LocalDateTime.now());
        workerNotificationMapper.insertWorkerNotification(
                auth.getWorkerId(),
                "REAL_NAME_APPROVED",
                "system",
                "实名认证已通过",
                "您的实名认证已通过审核",
                "REAL_NAME_AUTH",
                id);
    }

    @Override
    public void reject(Long id, Long reviewerId, String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new RuntimeException("拒绝原因必填");
        }
        WorkerRealNameAuth auth = mapper.findById(id)
                .orElseThrow(() -> new RuntimeException("记录不存在"));
        if (!"PENDING".equals(auth.getStatus())) {
            throw new RuntimeException("状态不允许操作");
        }
        mapper.updateReview(id, "REJECTED", reason, reviewerId, LocalDateTime.now());
        workerNotificationMapper.insertWorkerNotification(
                auth.getWorkerId(),
                "REAL_NAME_REJECTED",
                "system",
                "实名认证未通过",
                "您的实名认证未通过审核，原因：" + reason,
                "REAL_NAME_AUTH",
                id);
    }

    private Map<Long, Worker> loadWorkerMap(List<WorkerRealNameAuth> records) {
        List<Long> workerIds = records.stream()
                .map(WorkerRealNameAuth::getWorkerId)
                .filter(id -> id != null)
                .distinct()
                .toList();
        if (workerIds.isEmpty()) return Map.of();
        return workerMapper.findByIds(workerIds).stream()
                .collect(Collectors.toMap(Worker::getId, w -> w, (a, b) -> a));
    }

    private WorkerRealNameAuthVO toVO(WorkerRealNameAuth entity) {
        WorkerRealNameAuthVO vo = new WorkerRealNameAuthVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
