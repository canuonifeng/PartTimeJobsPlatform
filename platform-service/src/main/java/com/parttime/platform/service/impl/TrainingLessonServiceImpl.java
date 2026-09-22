package com.parttime.platform.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.parttime.platform.exception.BusinessException;
import com.parttime.platform.mapper.QuestionBankMapper;
import com.parttime.platform.mapper.TrainingLessonMapper;
import com.parttime.platform.pojo.cmd.TrainingLessonCreateCmd;
import com.parttime.platform.pojo.cmd.TrainingLessonQueryCmd;
import com.parttime.platform.pojo.cmd.TrainingLessonUpdateCmd;
import com.parttime.platform.pojo.entity.QuestionBank;
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
    private static final String EXAM = "EXAM";
    private static final String VIDEO = "VIDEO";
    private static final String AUDIO = "AUDIO";
    private static final String DOCUMENT = "DOCUMENT";
    private static final String IMAGE_TEXT = "IMAGE_TEXT";
    private static final Set<String> VALID_TYPES = Set.of(VIDEO, AUDIO, DOCUMENT, IMAGE_TEXT, EXAM);
    private static final Set<String> VALID_QUESTION_TYPES = Set.of("SINGLE_CHOICE", "MULTIPLE_CHOICE", "JUDGE");

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    private TrainingLessonMapper trainingLessonMapper;

    @Resource
    private QuestionBankMapper questionBankMapper;

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
        validateLessonContent(cmd.getLessonType(), cmd.getMediaUrl(), cmd.getContent(), cmd.getDurationMinutes());
        String examConfigJson = null;
        if (EXAM.equals(cmd.getLessonType())) {
            validateExamConfig(cmd.getExamConfig());
            examConfigJson = toExamConfigJson(cmd.getExamConfig());
        }
        TrainingLesson lesson = new TrainingLesson();
        lesson.setCourseId(cmd.getCourseId());
        lesson.setLessonType(cmd.getLessonType());
        lesson.setTitle(cmd.getTitle().trim());
        lesson.setContent(cmd.getContent());
        lesson.setMediaUrl(cmd.getMediaUrl());
        lesson.setDurationMinutes(cmd.getDurationMinutes());
        lesson.setExamConfigJson(examConfigJson);
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
        validateLessonContent(cmd.getLessonType(), cmd.getMediaUrl(), cmd.getContent(), cmd.getDurationMinutes());
        String examConfigJson = null;
        if (EXAM.equals(cmd.getLessonType())) {
            validateExamConfigUpdate(cmd.getExamConfig());
            examConfigJson = toExamConfigJson(cmd.getExamConfig());
        }
        lesson.setCourseId(cmd.getCourseId());
        lesson.setLessonType(cmd.getLessonType());
        lesson.setTitle(cmd.getTitle().trim());
        lesson.setContent(cmd.getContent());
        lesson.setMediaUrl(cmd.getMediaUrl());
        lesson.setDurationMinutes(cmd.getDurationMinutes());
        lesson.setExamConfigJson(examConfigJson);
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

    private void validateLessonContent(String lessonType, String mediaUrl, String content, Integer durationMinutes) {
        if (VIDEO.equals(lessonType) || AUDIO.equals(lessonType)) {
            if (mediaUrl == null || mediaUrl.isBlank()) {
                throw new BusinessException("音视频课时必须填写媒体地址");
            }
            if (durationMinutes == null || durationMinutes <= 0) {
                throw new BusinessException("课时时长必须大于0");
            }
        }
        if (DOCUMENT.equals(lessonType) || IMAGE_TEXT.equals(lessonType)) {
            if (content == null || content.isBlank()) {
                throw new BusinessException("文档课时必须填写正文内容");
            }
        }
    }

    private void validateExamConfig(TrainingLessonCreateCmd.ExamConfigDTO config) {
        if (config == null) {
            throw new BusinessException("考试课时必须配置考试规则");
        }
        validateExamConfigCommon(config.getBankId(), config.getDurationMinutes(),
                config.getPassScore(), config.getRules());
    }

    private void validateExamConfigUpdate(TrainingLessonUpdateCmd.ExamConfigDTO config) {
        if (config == null) {
            throw new BusinessException("考试课时必须配置考试规则");
        }
        validateExamConfigCommon(config.getBankId(), config.getDurationMinutes(),
                config.getPassScore(), config.getRules());
    }

    private void validateExamConfigCommon(Long bankId, Integer durationMinutes,
                                           Integer passScore, List<?> rules) {
        if (bankId == null) {
            throw new BusinessException("考试课时必须选择题库");
        }
        QuestionBank bank = questionBankMapper.findById(bankId)
                .orElseThrow(() -> new BusinessException("题库不存在或已停用"));
        if (!"ACTIVE".equals(bank.getStatus())) {
            throw new BusinessException("题库不存在或已停用");
        }
        if (durationMinutes == null || durationMinutes <= 0) {
            throw new BusinessException("考试时长必须大于0");
        }
        if (passScore == null || passScore < 0) {
            throw new BusinessException("及格分不能为负");
        }
        if (rules == null || rules.isEmpty()) {
            throw new BusinessException("考试规则不能为空");
        }
        for (Object rule : rules) {
            if (!isValidRule(rule)) {
                throw new BusinessException("题型规则不合法");
            }
        }
    }

    private boolean isValidRule(Object rule) {
        java.util.Map<?, ?> r;
        if (rule instanceof java.util.Map) {
            r = (java.util.Map<?, ?>) rule;
        } else {
            try {
                r = objectMapper.convertValue(rule, new TypeReference<java.util.Map<String, Object>>() {});
            } catch (Exception e) {
                return false;
            }
        }
        Object questionType = r.get("questionType");
        Object count = r.get("count");
        Object scorePer = r.get("scorePer");
        if (questionType == null || !VALID_QUESTION_TYPES.contains(questionType.toString())) {
            return false;
        }
        if (!(count instanceof Number) || ((Number) count).intValue() <= 0) {
            return false;
        }
        if (!(scorePer instanceof Number) || ((Number) scorePer).intValue() <= 0) {
            return false;
        }
        return true;
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
        TrainingLessonVO.ExamConfigVO examConfig = parseExamConfig(lesson.getExamConfigJson());
        vo.setExamConfig(examConfig);
        vo.setTotalScore(calcTotalScore(examConfig));
        vo.setSortOrder(lesson.getSortOrder());
        vo.setStatus(lesson.getStatus());
        vo.setCreatedAt(lesson.getCreatedAt());
        vo.setUpdatedAt(lesson.getUpdatedAt());
        return vo;
    }

    private Integer calcTotalScore(TrainingLessonVO.ExamConfigVO examConfig) {
        if (examConfig == null || examConfig.getRules() == null) {
            return 0;
        }
        int total = 0;
        for (TrainingLessonVO.ExamRuleVO rule : examConfig.getRules()) {
            if (rule.getCount() != null && rule.getScorePer() != null) {
                total += rule.getCount() * rule.getScorePer();
            }
        }
        return total;
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
