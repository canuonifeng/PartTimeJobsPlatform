package com.parttime.cservice.service.impl;

import com.parttime.cservice.mapper.WorkerRealNameAuthMapper;
import com.parttime.cservice.pojo.cmd.WorkerRealNameSubmitCmd;
import com.parttime.cservice.pojo.entity.WorkerRealNameAuth;
import com.parttime.cservice.pojo.vo.WorkerRealNameAuthVO;
import com.parttime.cservice.service.WorkerRealNameAuthService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class WorkerRealNameAuthServiceImpl implements WorkerRealNameAuthService {

    @Resource
    private WorkerRealNameAuthMapper workerRealNameAuthMapper;

    @Override
    public WorkerRealNameAuthVO submit(Long workerId, WorkerRealNameSubmitCmd cmd) {
        if (cmd == null || isBlank(cmd.getRealName()) || isBlank(cmd.getIdCardNo())) {
            throw new RuntimeException("姓名和身份证号必填");
        }
        Optional<WorkerRealNameAuth> existing = workerRealNameAuthMapper.findByWorkerId(workerId);
        if (existing.isPresent()) {
            WorkerRealNameAuth current = existing.get();
            if ("PENDING".equals(current.getStatus())) {
                throw new RuntimeException("已提交，请等待审核");
            }
            if ("APPROVED".equals(current.getStatus())) {
                throw new RuntimeException("已通过认证");
            }
            current.setRealName(cmd.getRealName());
            current.setIdCardNo(cmd.getIdCardNo());
            current.setIdCardFrontUrl(cmd.getIdCardFrontUrl());
            current.setIdCardBackUrl(cmd.getIdCardBackUrl());
            current.setStatus("PENDING");
            current.setRejectReason(null);
            current.setSubmittedAt(LocalDateTime.now());
            current.setReviewedAt(null);
            current.setReviewerId(null);
            workerRealNameAuthMapper.update(current);
            return toVO(current);
        }
        WorkerRealNameAuth auth = new WorkerRealNameAuth();
        auth.setWorkerId(workerId);
        auth.setRealName(cmd.getRealName());
        auth.setIdCardNo(cmd.getIdCardNo());
        auth.setIdCardFrontUrl(cmd.getIdCardFrontUrl());
        auth.setIdCardBackUrl(cmd.getIdCardBackUrl());
        auth.setStatus("PENDING");
        auth.setSubmittedAt(LocalDateTime.now());
        workerRealNameAuthMapper.insert(auth);
        return toVO(auth);
    }

    @Override
    public WorkerRealNameAuthVO getStatus(Long workerId) {
        Optional<WorkerRealNameAuth> existing = workerRealNameAuthMapper.findByWorkerId(workerId);
        if (existing.isEmpty()) {
            WorkerRealNameAuthVO vo = new WorkerRealNameAuthVO();
            vo.setStatus("NONE");
            return vo;
        }
        return toVO(existing.get());
    }

    private WorkerRealNameAuthVO toVO(WorkerRealNameAuth auth) {
        WorkerRealNameAuthVO vo = new WorkerRealNameAuthVO();
        vo.setStatus(auth.getStatus());
        vo.setRealName(auth.getRealName());
        vo.setIdCardNoMasked(maskIdCard(auth.getIdCardNo()));
        vo.setRejectReason(auth.getRejectReason());
        vo.setSubmittedAt(auth.getSubmittedAt());
        vo.setReviewedAt(auth.getReviewedAt());
        return vo;
    }

    private String maskIdCard(String s) {
        if (s == null || s.length() < 8) return s;
        return s.substring(0, 4) + "********" + s.substring(s.length() - 4);
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
