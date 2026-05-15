package com.parttime.cservice.service.impl;

import com.parttime.cservice.config.JwtTokenProvider;
import com.parttime.cservice.mapper.WorkerMapper;
import com.parttime.cservice.pojo.cmd.RegisterCmd;
import com.parttime.cservice.pojo.entity.Worker;
import com.parttime.cservice.pojo.vo.LoginVO;
import com.parttime.cservice.pojo.vo.WorkerVO;
import com.parttime.cservice.service.WorkerService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class WorkerServiceImpl implements WorkerService {

    @Resource
    private WorkerMapper workerMapper;
    @Resource
    private JwtTokenProvider jwtTokenProvider;

    @Override
    public WorkerVO register(RegisterCmd request) {
        Worker worker = new Worker();
        worker.setName(request.name());
        worker.setPhone(request.phone());
        worker.setAvatar(request.avatar());
        worker.setAvatarUrl(request.avatar());
        worker.setWechatCode("wx_" + System.currentTimeMillis());
        worker.setStatus("ACTIVE");
        worker.setCreatedAt(LocalDateTime.now());
        worker.setUpdatedAt(LocalDateTime.now());
        workerMapper.insert(worker);
        return toResponse(worker);
    }

    @Override
    public String login(String wechatCode) {
        Optional<Worker> existing = workerMapper.findByWechatCode(wechatCode);
        Long workerId;
        if (existing.isPresent()) {
            workerId = existing.get().getId();
        } else {
            Worker worker = new Worker();
            worker.setWechatCode(wechatCode);
            worker.setStatus("ACTIVE");
            worker.setCreatedAt(LocalDateTime.now());
            worker.setUpdatedAt(LocalDateTime.now());
            workerMapper.insert(worker);
            workerId = worker.getId();
        }
        return jwtTokenProvider.generateToken(String.valueOf(workerId), List.of("ROLE_WORKER"));
    }

    @Override
    public LoginVO loginWithWechat(String code) {
        String openId = exchangeWechatCode(code);
        Optional<Worker> existing = workerMapper.findByOpenId(openId);
        Long workerId;
        Worker worker;
        if (existing.isPresent()) {
            worker = existing.get();
            workerId = worker.getId();
        } else {
            worker = new Worker();
            worker.setOpenId(openId);
            worker.setStatus("ACTIVE");
            worker.setCreatedAt(LocalDateTime.now());
            worker.setUpdatedAt(LocalDateTime.now());
            workerMapper.insert(worker);
            workerId = worker.getId();
        }
        String token = jwtTokenProvider.generateToken(String.valueOf(workerId), List.of("ROLE_WORKER"));
        return new LoginVO(token, workerId, openId, worker.getName());
    }

    @Override
    public WorkerVO getWorkerByOpenId(String openId) {
        Worker worker = workerMapper.findByOpenId(openId)
                .orElseThrow(() -> new RuntimeException("Worker not found with openId: " + openId));
        return toResponse(worker);
    }

    @Override
    public WorkerVO getWorkerById(Long id) {
        Worker worker = workerMapper.findById(id)
                .orElseThrow(() -> new RuntimeException("Worker not found with id: " + id));
        return toResponse(worker);
    }

    @Override
    public WorkerVO updateProfile(Long id, RegisterCmd request) {
        Worker worker = workerMapper.findById(id)
                .orElseThrow(() -> new RuntimeException("Worker not found with id: " + id));
        if (request.name() != null) {
            worker.setName(request.name());
        }
        if (request.phone() != null) {
            worker.setPhone(request.phone());
        }
        if (request.avatar() != null) {
            worker.setAvatar(request.avatar());
            worker.setAvatarUrl(request.avatar());
        }
        worker.setUpdatedAt(LocalDateTime.now());
        workerMapper.update(worker);
        return toResponse(worker);
    }

    private String exchangeWechatCode(String code) {
        return "openid_" + code;
    }

    private WorkerVO toResponse(Worker worker) {
        WorkerVO response = new WorkerVO();
        response.setId(worker.getId());
        response.setName(worker.getName());
        response.setPhone(worker.getPhone());
        response.setAvatar(worker.getAvatar());
        response.setOpenId(worker.getOpenId());
        response.setNickname(worker.getNickname());
        response.setAvatarUrl(worker.getAvatarUrl());
        response.setCreatedAt(worker.getCreatedAt());
        return response;
    }
}
