package com.parttime.cservice.core.service;

import com.parttime.cservice.core.auth.JwtTokenProvider;
import com.parttime.cservice.core.dto.WeChatLoginRequest;
import com.parttime.cservice.core.dto.WeChatLoginResponse;
import com.parttime.cservice.core.dto.WorkerRegisterRequest;
import com.parttime.cservice.core.dto.WorkerResponse;
import com.parttime.cservice.core.model.Worker;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class WorkerService {

    private final ConcurrentHashMap<Long, Worker> workers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> wechatCodeIndex = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> openIdIndex = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);
    private final JwtTokenProvider jwtTokenProvider;

    public WorkerService(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public WorkerResponse register(WorkerRegisterRequest request) {
        Worker worker = new Worker(idCounter.getAndIncrement(), request.name(), request.phone(),
                request.avatar(), "wx_" + System.currentTimeMillis(), LocalDateTime.now());
        workers.put(worker.getId(), worker);
        return toResponse(worker);
    }

    public String login(String wechatCode) {
        Long workerId = wechatCodeIndex.computeIfAbsent(wechatCode, code -> {
            Worker worker = new Worker(idCounter.getAndIncrement(), null, null, null, code, LocalDateTime.now());
            workers.put(worker.getId(), worker);
            return worker.getId();
        });
        return jwtTokenProvider.generateToken(String.valueOf(workerId), List.of("ROLE_WORKER"));
    }

    public WeChatLoginResponse loginWithWechat(String code) {
        String openId = exchangeWechatCode(code);
        Long workerId = openIdIndex.computeIfAbsent(openId, id -> {
            Worker worker = new Worker(idCounter.getAndIncrement(), null, null, null, openId, null, null, LocalDateTime.now());
            workers.put(worker.getId(), worker);
            return worker.getId();
        });
        Worker worker = workers.get(workerId);
        String token = jwtTokenProvider.generateToken(String.valueOf(workerId), List.of("ROLE_WORKER"));
        return new WeChatLoginResponse(token, workerId, openId, worker.getNickname());
    }

    public WorkerResponse getWorkerByOpenId(String openId) {
        Long workerId = openIdIndex.get(openId);
        if (workerId == null) {
            throw new RuntimeException("Worker not found with openId: " + openId);
        }
        return getWorkerById(workerId);
    }

    public WorkerResponse getWorkerById(Long id) {
        Worker worker = workers.get(id);
        if (worker == null) {
            throw new RuntimeException("Worker not found with id: " + id);
        }
        return toResponse(worker);
    }

    public WorkerResponse updateProfile(Long id, WorkerRegisterRequest request) {
        Worker worker = workers.get(id);
        if (worker == null) {
            throw new RuntimeException("Worker not found with id: " + id);
        }
        if (request.name() != null) {
            worker.setName(request.name());
        }
        if (request.phone() != null) {
            worker.setPhone(request.phone());
        }
        if (request.avatar() != null) {
            worker.setAvatar(request.avatar());
        }
        return toResponse(worker);
    }

    private String exchangeWechatCode(String code) {
        return "openid_" + code;
    }

    private WorkerResponse toResponse(Worker worker) {
        WorkerResponse response = new WorkerResponse();
        response.setId(worker.getId());
        response.setName(worker.getName());
        response.setPhone(worker.getPhone());
        response.setAvatar(worker.getAvatar());
        response.setOpenId(worker.getWechatOpenId());
        response.setNickname(worker.getNickname());
        response.setAvatarUrl(worker.getAvatarUrl());
        response.setCreatedAt(worker.getCreatedAt());
        return response;
    }
}
