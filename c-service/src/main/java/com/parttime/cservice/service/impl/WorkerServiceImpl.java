package com.parttime.cservice.service.impl;

import com.parttime.cservice.config.JwtTokenProvider;
import com.parttime.cservice.pojo.cmd.RegisterCmd;
import com.parttime.cservice.pojo.cmd.WeChatLoginCmd;
import com.parttime.cservice.pojo.entity.Worker;
import com.parttime.cservice.pojo.vo.LoginVO;
import com.parttime.cservice.pojo.vo.WorkerVO;
import com.parttime.cservice.service.WorkerService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class WorkerServiceImpl implements WorkerService {

    private final ConcurrentHashMap<Long, Worker> workers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> wechatCodeIndex = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> openIdIndex = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);
    private final JwtTokenProvider jwtTokenProvider;

    public WorkerServiceImpl(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public WorkerVO register(RegisterCmd request) {
        Worker worker = new Worker(idCounter.getAndIncrement(), request.name(), request.phone(),
                request.avatar(), "wx_" + System.currentTimeMillis(), LocalDateTime.now());
        workers.put(worker.getId(), worker);
        return toResponse(worker);
    }

    @Override
    public String login(String wechatCode) {
        Long workerId = wechatCodeIndex.computeIfAbsent(wechatCode, code -> {
            Worker worker = new Worker(idCounter.getAndIncrement(), null, null, null, code, LocalDateTime.now());
            workers.put(worker.getId(), worker);
            return worker.getId();
        });
        return jwtTokenProvider.generateToken(String.valueOf(workerId), List.of("ROLE_WORKER"));
    }

    @Override
    public LoginVO loginWithWechat(String code) {
        String openId = exchangeWechatCode(code);
        Long workerId = openIdIndex.computeIfAbsent(openId, id -> {
            Worker worker = new Worker(idCounter.getAndIncrement(), null, null, null, openId, null, null, LocalDateTime.now());
            workers.put(worker.getId(), worker);
            return worker.getId();
        });
        Worker worker = workers.get(workerId);
        String token = jwtTokenProvider.generateToken(String.valueOf(workerId), List.of("ROLE_WORKER"));
        return new LoginVO(token, workerId, openId, worker.getNickname());
    }

    @Override
    public WorkerVO getWorkerByOpenId(String openId) {
        Long workerId = openIdIndex.get(openId);
        if (workerId == null) {
            throw new RuntimeException("Worker not found with openId: " + openId);
        }
        return getWorkerById(workerId);
    }

    @Override
    public WorkerVO getWorkerById(Long id) {
        Worker worker = workers.get(id);
        if (worker == null) {
            throw new RuntimeException("Worker not found with id: " + id);
        }
        return toResponse(worker);
    }

    @Override
    public WorkerVO updateProfile(Long id, RegisterCmd request) {
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

    private WorkerVO toResponse(Worker worker) {
        WorkerVO response = new WorkerVO();
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
