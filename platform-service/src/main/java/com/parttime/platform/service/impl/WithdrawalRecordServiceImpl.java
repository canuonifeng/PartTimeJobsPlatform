package com.parttime.platform.service.impl;

import com.parttime.platform.mapper.WithdrawalRecordMapper;
import com.parttime.platform.mapper.WorkerMapper;
import com.parttime.platform.pojo.entity.WithdrawalRecord;
import com.parttime.platform.pojo.entity.Worker;
import com.parttime.platform.pojo.vo.PageVO;
import com.parttime.platform.pojo.vo.WithdrawalRecordVO;
import com.parttime.platform.service.WithdrawalRecordService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WithdrawalRecordServiceImpl implements WithdrawalRecordService {

    @Resource
    private WithdrawalRecordMapper withdrawalRecordMapper;

    @Resource
    private WorkerMapper workerMapper;

    @Override
    public PageVO<WithdrawalRecordVO> listRecords(Long workerId, String status, LocalDateTime startTime, LocalDateTime endTime, int page, int pageSize) {
        int offset = Math.max(page - 1, 0) * pageSize;
        List<WithdrawalRecord> records = withdrawalRecordMapper.findPage(workerId, status, startTime, endTime, offset, pageSize);
        Map<Long, Worker> workerMap = loadWorkerMap(records);
        List<WithdrawalRecordVO> vos = records.stream()
                .map(r -> {
                    WithdrawalRecordVO vo = toVO(r);
                    Worker worker = workerMap.get(r.getWorkerId());
                    if (worker != null) {
                        vo.setWorkerName(worker.getName());
                        vo.setWorkerPhone(worker.getPhone());
                    }
                    return vo;
                })
                .collect(Collectors.toList());
        long total = withdrawalRecordMapper.countPage(workerId, status, startTime, endTime);
        return new PageVO<>(vos, total);
    }

    private Map<Long, Worker> loadWorkerMap(List<WithdrawalRecord> records) {
        List<Long> workerIds = records.stream()
                .map(WithdrawalRecord::getWorkerId)
                .filter(id -> id != null)
                .distinct()
                .toList();
        if (workerIds.isEmpty()) return Map.of();
        return workerMapper.findByIds(workerIds).stream()
                .collect(Collectors.toMap(Worker::getId, w -> w, (a, b) -> a));
    }

    private WithdrawalRecordVO toVO(WithdrawalRecord record) {
        WithdrawalRecordVO vo = new WithdrawalRecordVO();
        BeanUtils.copyProperties(record, vo);
        return vo;
    }
}
