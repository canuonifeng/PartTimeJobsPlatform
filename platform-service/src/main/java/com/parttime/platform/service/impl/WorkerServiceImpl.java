package com.parttime.platform.service.impl;
import com.parttime.platform.exception.BusinessException;
import com.parttime.platform.mapper.WorkerMapper;
import com.parttime.platform.pojo.cmd.WorkerUpdateCmd;
import com.parttime.platform.pojo.entity.Worker;
import com.parttime.platform.pojo.vo.WorkerVO;
import com.parttime.platform.service.WorkerService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorkerServiceImpl implements WorkerService {

    @Resource
    private WorkerMapper workerMapper;

    @Override
    public List<WorkerVO> list(String status, String keyword) {
        List<Worker> list = workerMapper.findAll(status, keyword);
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public WorkerVO detail(Long id) {
        Worker w = workerMapper.findById(id)
                .orElseThrow(() -> new BusinessException("Worker not found: " + id));
        return toVO(w);
    }

    @Override
    public WorkerVO update(WorkerUpdateCmd cmd) {
        Worker w = workerMapper.findById(cmd.getId())
                .orElseThrow(() -> new BusinessException("Worker not found: " + cmd.getId()));
        if (cmd.getName() != null) w.setName(cmd.getName());
        if (cmd.getPhone() != null) w.setPhone(cmd.getPhone());
        workerMapper.update(w);
        return toVO(w);
    }

    @Override
    public void ban(Long id) {
        Worker w = workerMapper.findById(id)
                .orElseThrow(() -> new BusinessException("Worker not found: " + id));
        if (!"ACTIVE".equals(w.getStatus())) {
            throw new BusinessException("Worker is not ACTIVE");
        }
        workerMapper.updateStatus(id, "DISABLED");
    }

    @Override
    public void unban(Long id) {
        Worker w = workerMapper.findById(id)
                .orElseThrow(() -> new BusinessException("Worker not found: " + id));
        if (!"DISABLED".equals(w.getStatus())) {
            throw new BusinessException("Worker is not DISABLED");
        }
        workerMapper.updateStatus(id, "ACTIVE");
    }

    private WorkerVO toVO(Worker w) {
        WorkerVO vo = new WorkerVO();
        vo.setId(w.getId());
        vo.setName(w.getName());
        vo.setPhone(w.getPhone());
        vo.setWechatCode(w.getWechatCode());
        vo.setOpenId(w.getOpenId());
        vo.setAvatarUrl(w.getAvatarUrl());
        vo.setStatus(w.getStatus());
        vo.setCreatedAt(w.getCreatedAt());
        return vo;
    }
}
