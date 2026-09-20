package com.parttime.platform.service.impl;

import com.parttime.platform.exception.BusinessException;
import com.parttime.platform.mapper.OperationActivityMapper;
import com.parttime.platform.mapper.PushTaskMapper;
import com.parttime.platform.pojo.cmd.ActivityCmd;
import com.parttime.platform.pojo.cmd.ActivityToggleCmd;
import com.parttime.platform.pojo.cmd.IdCmd;
import com.parttime.platform.pojo.cmd.PushTaskCmd;
import com.parttime.platform.pojo.entity.OperationActivity;
import com.parttime.platform.pojo.entity.PushTask;
import com.parttime.platform.pojo.vo.ActivityEffectVO;
import com.parttime.platform.pojo.vo.OperationActivityVO;
import com.parttime.platform.pojo.vo.PushTaskVO;
import com.parttime.platform.service.ActivityService;
import jakarta.annotation.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ActivityServiceImpl implements ActivityService {

    @Resource
    private OperationActivityMapper activityMapper;

    @Resource
    private PushTaskMapper pushTaskMapper;

    @Override
    public List<OperationActivityVO> list() {
        return activityMapper.findAll().stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public void create(ActivityCmd cmd) {
        OperationActivity activity = new OperationActivity();
        applyCmd(activity, cmd);
        activity.setStatus(cmd.getStatus() == null ? "DRAFT" : cmd.getStatus());
        activity.setOperatorName(currentOperatorName());
        activityMapper.insert(activity);
    }

    @Override
    public void update(ActivityCmd cmd) {
        OperationActivity activity = activityMapper.findById(cmd.getId())
                .orElseThrow(() -> new BusinessException("活动不存在: " + cmd.getId()));
        applyCmd(activity, cmd);
        if (cmd.getStatus() != null) {
            activity.setStatus(cmd.getStatus());
        }
        activityMapper.update(activity);
    }

    @Override
    public void toggle(ActivityToggleCmd cmd) {
        OperationActivity activity = activityMapper.findById(cmd.getId())
                .orElseThrow(() -> new BusinessException("活动不存在: " + cmd.getId()));
        activityMapper.updateStatus(activity.getId(), cmd.getStatus());
    }

    @Override
    public void delete(IdCmd cmd) {
        activityMapper.findById(cmd.getId())
                .orElseThrow(() -> new BusinessException("活动不存在: " + cmd.getId()));
        activityMapper.deleteById(cmd.getId());
    }

    @Override
    public ActivityEffectVO effectStats(IdCmd cmd) {
        OperationActivity activity = activityMapper.findById(cmd.getId())
                .orElseThrow(() -> new BusinessException("活动不存在: " + cmd.getId()));
        ActivityEffectVO vo = new ActivityEffectVO();
        vo.setActivityId(activity.getId());
        vo.setParticipantCount(activity.getParticipantCount() == null ? 0 : activity.getParticipantCount());
        vo.setViewCount(activity.getViewCount() == null ? 0 : activity.getViewCount());
        Long incrApplications = 0L;
        BigDecimal conversionRate = BigDecimal.ZERO;
        if (activity.getStartTime() != null && activity.getEndTime() != null) {
            incrApplications = activityMapper.countApplicationsByDateRange(
                    activity.getStartTime().toString(), activity.getEndTime().toString());
            vo.setIncrementalApplications(incrApplications);
            long views = activity.getViewCount() == null ? 0 : activity.getViewCount();
            if (views > 0) {
                conversionRate = BigDecimal.valueOf(incrApplications)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(views), 2, RoundingMode.HALF_UP);
            }
        } else {
            vo.setIncrementalApplications(0L);
        }
        vo.setConversionRate(conversionRate);
        return vo;
    }

    @Override
    public List<PushTaskVO> pushTasks() {
        return pushTaskMapper.findAll().stream().map(this::toPushVO).collect(Collectors.toList());
    }

    @Override
    public void createPush(PushTaskCmd cmd) {
        PushTask task = new PushTask();
        task.setTitle(cmd.getTitle());
        task.setContent(cmd.getContent());
        task.setTargetType(cmd.getTargetType() == null ? "ALL" : cmd.getTargetType());
        task.setTargetIds(cmd.getTargetIds());
        task.setPushType(cmd.getPushType() == null ? "IMMEDIATE" : cmd.getPushType());
        task.setScheduledTime(cmd.getScheduledTime());
        task.setRemark(cmd.getRemark());
        task.setTotalCount(estimateTargetCount(task.getTargetType(), task.getTargetIds()));
        task.setOperatorName(currentOperatorName());
        pushTaskMapper.insert(task);
    }

    private Integer estimateTargetCount(String targetType, String targetIds) {
        if ("CUSTOM".equals(targetType) && targetIds != null && !targetIds.isBlank()) {
            return targetIds.split(",").length;
        }
        if ("ALL".equals(targetType)) {
            return 0;
        }
        return 0;
    }

    private void applyCmd(OperationActivity activity, ActivityCmd cmd) {
        activity.setTitle(cmd.getTitle());
        activity.setDescription(cmd.getDescription());
        activity.setActivityType(cmd.getActivityType() == null ? "GENERAL" : cmd.getActivityType());
        activity.setBannerImage(cmd.getBannerImage());
        activity.setContentImage(cmd.getContentImage());
        activity.setLinkUrl(cmd.getLinkUrl());
        activity.setStartTime(cmd.getStartTime());
        activity.setEndTime(cmd.getEndTime());
        activity.setSortOrder(cmd.getSortOrder() == null ? 0 : cmd.getSortOrder());
    }

    private OperationActivityVO toVO(OperationActivity a) {
        OperationActivityVO vo = new OperationActivityVO();
        vo.setId(a.getId());
        vo.setTitle(a.getTitle());
        vo.setDescription(a.getDescription());
        vo.setActivityType(a.getActivityType());
        vo.setBannerImage(a.getBannerImage());
        vo.setContentImage(a.getContentImage());
        vo.setLinkUrl(a.getLinkUrl());
        vo.setStatus(a.getStatus());
        vo.setStartTime(a.getStartTime());
        vo.setEndTime(a.getEndTime());
        vo.setParticipantCount(a.getParticipantCount());
        vo.setViewCount(a.getViewCount());
        vo.setSortOrder(a.getSortOrder());
        vo.setOperatorName(a.getOperatorName());
        vo.setCreatedAt(a.getCreatedAt());
        return vo;
    }

    private PushTaskVO toPushVO(PushTask t) {
        PushTaskVO vo = new PushTaskVO();
        vo.setId(t.getId());
        vo.setTitle(t.getTitle());
        vo.setContent(t.getContent());
        vo.setTargetType(t.getTargetType());
        vo.setPushType(t.getPushType());
        vo.setScheduledTime(t.getScheduledTime());
        vo.setStatus(t.getStatus());
        vo.setTotalCount(t.getTotalCount());
        vo.setSuccessCount(t.getSuccessCount());
        vo.setFailCount(t.getFailCount());
        vo.setOperatorName(t.getOperatorName());
        vo.setCompletedAt(t.getCompletedAt());
        vo.setRemark(t.getRemark());
        vo.setCreatedAt(t.getCreatedAt());
        return vo;
    }

    private String currentOperatorName() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return "system";
        }
        String name = auth.getName();
        return name == null || name.isBlank() ? "system" : name;
    }
}
