package com.parttime.platform.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.parttime.platform.exception.BusinessException;
import com.parttime.platform.mapper.TrainingLessonMapper;
import com.parttime.platform.pojo.cmd.TrainingLessonCreateCmd;
import com.parttime.platform.pojo.cmd.TrainingLessonQueryCmd;
import com.parttime.platform.pojo.cmd.TrainingLessonUpdateCmd;
import com.parttime.platform.pojo.entity.TrainingLesson;
import com.parttime.platform.pojo.vo.TrainingLessonVO;
import com.parttime.platform.service.TrainingLessonService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TrainingLessonServiceImpl implements TrainingLessonService {

    private static final Logger log = LoggerFactory.getLogger(TrainingLessonServiceImpl.class);

    private static final String DRAFT = "DRAFT";
    private static final String PUBLISHED = "PUBLISHED";
    private static final String OFFLINE = "OFFLINE";
    private static final Set<String> VALID_TYPES = Set.of("VIDEO", "AUDIO", "DOCUMENT", "IMAGE_TEXT", "EXAM");

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    private TrainingLessonMapper trainingLessonMapper;

    @Override
    public List<TrainingLessonVO> list(TrainingLessonQueryCmd cmd) {
        if (cmd.getCourseId() == null) {
            throw new BusinessException("课程ID不能为空");
        }
        return trainingLessonMapper.findByCourseId(cmd.getCourseId()).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public TrainingLessonVO create(TrainingLessonCreateCmd cmd) {
        validate(cmd.getCourseId(), cmd.getLessonType(), cmd.getTitle());
        TrainingLesson lesson = new TrainingLesson();
        lesson.setCourseId(cmd.getCourseId());
        lesson.setLessonType(cmd.getLessonType());
        lesson.setTitle(cmd.getTitle().trim());
        lesson.setContent(cmd.getContent());
        lesson.setMediaUrl(cmd.getMediaUrl());
        lesson.setDurationMinutes(cmd.getDurationMinutes());
        lesson.setExamConfigJson(toExamConfigJson(cmd.getExamConfig()));
        lesson.setStatus(DRAFT);
        lesson.setSortOrder(cmd.getSortOrder() == null ? 0 : cmd.getSortOrder());
        trainingLessonMapper.insert(lesson);
        return toVO(lesson);
    }

    @Override
    public TrainingLessonVO update(TrainingLessonUpdateCmd cmd) {
        TrainingLesson lesson = trainingLessonMapper.findById(cmd.getId())
                .orElseThrow(() -> new BusinessException("课时不存在: " + cmd.getId()));
        validate(cmd.getCourseId(), cmd.getLessonType(), cmd.getTitle());
        lesson.setCourseId(cmd.getCourseId());
        lesson.setLessonType(cmd.getLessonType());
        lesson.setTitle(cmd.getTitle().trim());
        lesson.setContent(cmd.getContent());
        lesson.setMediaUrl(cmd.getMediaUrl());
        lesson.setDurationMinutes(cmd.getDurationMinutes());
        lesson.setExamConfigJson(toExamConfigJson(cmd.getExamConfig()));
        lesson.setSortOrder(cmd.getSortOrder() == null ? lesson.getSortOrder() : cmd.getSortOrder());
        trainingLessonMapper.update(lesson);
        return toVO(lesson);
    }

    @Override
    public void delete(Long id) {
        TrainingLesson lesson = trainingLessonMapper.findById(id)
                .orElseThrow(() -> new BusinessException("课时不存在: " + id));
        if (PUBLISHED.equals(lesson.getStatus())) {
            throw new BusinessException("已发布课时请先下线");
        }
        trainingLessonMapper.deleteById(id);
    }

    @Override
    public void publish(Long id) {
        TrainingLesson lesson = trainingLessonMapper.findById(id)
                .orElseThrow(() -> new BusinessException("课时不存在: " + id));
        if (!DRAFT.equals(lesson.getStatus())) {
            throw new BusinessException("仅草稿状态可发布");
        }
        trainingLessonMapper.updateStatus(id, PUBLISHED);
    }

    @Override
    public void offline(Long id) {
        TrainingLesson lesson = trainingLessonMapper.findById(id)
                .orElseThrow(() -> new BusinessException("课时不存在: " + id));
        if (!PUBLISHED.equals(lesson.getStatus())) {
            throw new BusinessException("仅已发布状态可下线");
        }
        trainingLessonMapper.updateStatus(id, OFFLINE);
    }

    private void validate(Long courseId, String lessonType, String title) {
        if (courseId == null) {
            throw new BusinessException("课程ID不能为空");
        }
        if (lessonType == null || !VALID_TYPES.contains(lessonType)) {
            throw new BusinessException("课时类型不合法");
        }
        if (title == null || title.isBlank()) {
            throw new BusinessException("课时标题不能为空");
        }
    }

    private String toExamConfigJson(Object examConfig) {
        if (examConfig == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(examConfig);
        } catch (Exception e) {
            log.warn("examConfig serialize failed", e);
            return null;
        }
    }

    private TrainingLessonVO toVO(TrainingLesson lesson) {
        TrainingLessonVO vo = new TrainingLessonVO();
        vo.setId(lesson.getId());
        vo.setCourseId(lesson.getCourseId());
        vo.setLessonType(lesson.getLessonType());
        vo.setTitle(lesson.getTitle());
        vo.setContent(lesson.getContent());
        vo.setMediaUrl(lesson.getMediaUrl());
        vo.setDurationMinutes(lesson.getDurationMinutes());
        vo.setExamConfig(parseExamConfig(lesson.getExamConfigJson()));
        vo.setTotalScore(null);
        vo.setSortOrder(lesson.getSortOrder());
        vo.setStatus(lesson.getStatus());
        vo.setCreatedAt(lesson.getCreatedAt());
        vo.setUpdatedAt(lesson.getUpdatedAt());
        return vo;
    }

    private TrainingLessonVO.ExamConfigVO parseExamConfig(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<TrainingLessonVO.ExamConfigVO>() {});
        } catch (Exception e) {
            log.warn("examConfig parse failed: {}", json, e);
            return null;
        }
    }
}
