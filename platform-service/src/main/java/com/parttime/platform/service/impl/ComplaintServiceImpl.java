package com.parttime.platform.service.impl;

import com.parttime.platform.exception.BusinessException;
import com.parttime.platform.mapper.ComplaintMapper;
import com.parttime.platform.pojo.cmd.ComplaintArbitrateCmd;
import com.parttime.platform.pojo.cmd.ComplaintHandleCmd;
import com.parttime.platform.pojo.cmd.ComplaintQueryCmd;
import com.parttime.platform.pojo.entity.Complaint;
import com.parttime.platform.pojo.vo.ComplaintVO;
import com.parttime.platform.service.ComplaintService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ComplaintServiceImpl implements ComplaintService {

    @Resource
    private ComplaintMapper complaintMapper;

    @Override
    public List<ComplaintVO> list(ComplaintQueryCmd cmd) {
        ComplaintQueryCmd q = cmd != null ? cmd : new ComplaintQueryCmd();
        List<Complaint> list = complaintMapper.findByFilters(q.getStatus(), q.getComplaintType(), q.getComplainantType(), q.getKeyword());
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public ComplaintVO detail(Long id) {
        Complaint complaint = complaintMapper.findById(id)
                .orElseThrow(() -> new BusinessException("投诉工单不存在: " + id));
        return toVO(complaint);
    }

    @Override
    public void handle(ComplaintHandleCmd cmd) {
        Complaint complaint = complaintMapper.findById(cmd.getId())
                .orElseThrow(() -> new BusinessException("投诉工单不存在: " + cmd.getId()));
        if ("CLOSED".equals(complaint.getStatus()) || "RESOLVED".equals(complaint.getStatus())) {
            throw new BusinessException("当前工单状态不可处理: " + complaint.getStatus());
        }
        complaintMapper.handle(cmd.getId(), cmd.getHandlerName(), cmd.getHandleResult());
    }

    @Override
    public void arbitrate(ComplaintArbitrateCmd cmd) {
        Complaint complaint = complaintMapper.findById(cmd.getId())
                .orElseThrow(() -> new BusinessException("投诉工单不存在: " + cmd.getId()));
        if ("CLOSED".equals(complaint.getStatus())) {
            throw new BusinessException("已关闭工单不可仲裁");
        }
        complaintMapper.arbitrate(cmd.getId(), cmd.getHandlerName(), cmd.getConclusion());
    }

    @Override
    public void close(Long id) {
        Complaint complaint = complaintMapper.findById(id)
                .orElseThrow(() -> new BusinessException("投诉工单不存在: " + id));
        complaintMapper.close(complaint.getId());
    }

    private ComplaintVO toVO(Complaint c) {
        ComplaintVO vo = new ComplaintVO();
        vo.setId(c.getId());
        vo.setComplaintNo(c.getComplaintNo());
        vo.setComplainantType(c.getComplainantType());
        vo.setComplainantId(c.getComplainantId());
        vo.setComplainantName(c.getComplainantName());
        vo.setComplainantPhone(c.getComplainantPhone());
        vo.setAccusedType(c.getAccusedType());
        vo.setAccusedId(c.getAccusedId());
        vo.setAccusedName(c.getAccusedName());
        vo.setComplaintType(c.getComplaintType());
        vo.setTitle(c.getTitle());
        vo.setContent(c.getContent());
        vo.setImages(c.getImages());
        vo.setRelatedJobId(c.getRelatedJobId());
        vo.setRelatedJobTitle(c.getRelatedJobTitle());
        vo.setRelatedScheduleId(c.getRelatedScheduleId());
        vo.setStatus(c.getStatus());
        vo.setPriority(c.getPriority());
        vo.setHandlerName(c.getHandlerName());
        vo.setHandleResult(c.getHandleResult());
        vo.setHandledAt(c.getHandledAt());
        vo.setSatisfactionScore(c.getSatisfactionScore());
        vo.setIsAppeal(c.getIsAppeal());
        vo.setParentId(c.getParentId());
        vo.setRemark(c.getRemark());
        vo.setCreatedAt(c.getCreatedAt());
        vo.setUpdatedAt(c.getUpdatedAt());
        return vo;
    }
}
