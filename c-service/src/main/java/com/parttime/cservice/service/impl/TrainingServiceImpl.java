package com.parttime.cservice.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.parttime.cservice.mapper.TrainingCertificationMapper;
import com.parttime.cservice.mapper.TrainingCourseMapper;
import com.parttime.cservice.mapper.TrainingLessonMapper;
import com.parttime.cservice.mapper.WorkerCertificationMapper;
import com.parttime.cservice.mapper.WorkerLessonRecordMapper;
import com.parttime.cservice.mapper.WorkerTrainingRecordMapper;
import com.parttime.cservice.pojo.cmd.ExamSubmitCmd;
import com.parttime.cservice.pojo.cmd.StartLessonCmd;
import com.parttime.cservice.pojo.entity.TrainingCertification;
import com.parttime.cservice.pojo.entity.TrainingCourse;
import com.parttime.cservice.pojo.entity.TrainingLesson;
import com.parttime.cservice.pojo.entity.WorkerCertification;
import com.parttime.cservice.pojo.entity.WorkerLessonRecord;
import com.parttime.cservice.pojo.entity.WorkerTrainingRecord;
import com.parttime.cservice.pojo.vo.ExamQuestionVO;
import com.parttime.cservice.pojo.vo.ExamResultVO;
import com.parttime.cservice.pojo.vo.LessonStartVO;
import com.parttime.cservice.pojo.vo.TrainingCourseDetailVO;
import com.parttime.cservice.pojo.vo.TrainingCourseVO;
import com.parttime.cservice.pojo.vo.TrainingLessonVO;
import com.parttime.cservice.pojo.vo.WorkerCertificationVO;
import com.parttime.cservice.service.TrainingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TrainingServiceImpl implements TrainingService {

    private static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_FAILED = "FAILED";
    private static final String CERT_ACTIVE = "ACTIVE";

    @Resource
    private TrainingCourseMapper trainingCourseMapper;

    @Resource
    private TrainingCertificationMapper trainingCertificationMapper;

    @Resource
    private TrainingLessonMapper trainingLessonMapper;

    @Resource
    private WorkerTrainingRecordMapper workerTrainingRecordMapper;

    @Resource
    private WorkerLessonRecordMapper workerLessonRecordMapper;

    @Resource
    private WorkerCertificationMapper workerCertificationMapper;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public List<TrainingCourseVO> listCourses(Long workerId) {
        List<TrainingCourse> courses = trainingCourseMapper.findPublished();
        if (courses.isEmpty()) {
            return List.of();
        }
        List<Long> courseIds = courses.stream().map(TrainingCourse::getId).toList();
        List<Long> certIds = courses.stream()
                .map(TrainingCourse::getCertificationId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, TrainingCertification> certMap = certIds.isEmpty()
                ? Map.of()
                : trainingCertificationMapper.findByIds(certIds).stream()
                        .collect(Collectors.toMap(TrainingCertification::getId, Function.identity()));
        List<TrainingLesson> lessons = trainingLessonMapper.findByCourseIds(courseIds);
        Map<Long, List<TrainingLesson>> lessonsByCourse = lessons.stream()
                .collect(Collectors.groupingBy(TrainingLesson::getCourseId));
        List<WorkerLessonRecord> myLessonRecords = workerId == null
                ? List.of()
                : workerLessonRecordMapper.findByWorkerId(workerId);
        Map<Long, WorkerLessonRecord> recordByLesson = myLessonRecords.stream()
                .collect(Collectors.toMap(WorkerLessonRecord::getLessonId, Function.identity(), (a, b) -> a));
        List<WorkerCertification> myCerts = workerId == null
                ? List.of()
                : workerCertificationMapper.findByWorkerId(workerId);
        Map<Long, WorkerCertification> certByCertId = myCerts.stream()
                .collect(Collectors.toMap(WorkerCertification::getCertificationId, Function.identity(), (a, b) -> a));

        return courses.stream().map(course -> {
            TrainingCourseVO vo = new TrainingCourseVO();
            vo.setId(course.getId());
            vo.setCertificationId(course.getCertificationId());
            TrainingCertification cert = certMap.get(course.getCertificationId());
            vo.setCertificationName(cert == null ? null : cert.getName());
            vo.setTitle(course.getTitle());
            vo.setSummary(course.getSummary());
            List<TrainingLesson> courseLessons = lessonsByCourse.getOrDefault(course.getId(), List.of());
            int total = courseLessons.size();
            int completed = 0;
            boolean anyProgress = false;
            for (TrainingLesson lesson : courseLessons) {
                WorkerLessonRecord record = recordByLesson.get(lesson.getId());
                if (record != null) {
                    anyProgress = true;
                    if (STATUS_COMPLETED.equals(record.getStatus())) {
                        completed++;
                    }
                }
            }
            vo.setTotalLessonCount(total);
            vo.setCompletedLessonCount(completed);
            if (total == 0) {
                vo.setMyStatus("NOT_STARTED");
            } else if (completed == total) {
                vo.setMyStatus(STATUS_COMPLETED);
            } else if (anyProgress) {
                vo.setMyStatus(STATUS_IN_PROGRESS);
            } else {
                vo.setMyStatus("NOT_STARTED");
            }
            vo.setMyScore(null);
            WorkerCertification wc = cert == null ? null : certByCertId.get(cert.getId());
            vo.setCertified(wc != null && CERT_ACTIVE.equals(wc.getStatus())
                    && (wc.getExpiresAt() == null || wc.getExpiresAt().isAfter(LocalDateTime.now())));
            return vo;
        }).toList();
    }

    @Override
    public TrainingCourseDetailVO getCourseDetail(Long workerId, Long courseId) {
        TrainingCourse course = trainingCourseMapper.findPublishedById(courseId)
                .orElseThrow(() -> new RuntimeException("课程不存在或未发布"));
        TrainingCertification cert = trainingCertificationMapper.findById(course.getCertificationId()).orElse(null);

        List<TrainingLesson> lessons = trainingLessonMapper.findByCourseId(courseId);
        Map<Long, WorkerLessonRecord> recordByLesson = (workerId == null
                ? List.<WorkerLessonRecord>of()
                : workerLessonRecordMapper.findByWorkerId(workerId)).stream()
                .collect(Collectors.toMap(WorkerLessonRecord::getLessonId, Function.identity(), (a, b) -> a));

        List<TrainingLessonVO> lessonVOs = new ArrayList<>();
        boolean prevCompleted = true;
        for (int i = 0; i < lessons.size(); i++) {
            TrainingLesson lesson = lessons.get(i);
            WorkerLessonRecord record = recordByLesson.get(lesson.getId());
            boolean completed = record != null && STATUS_COMPLETED.equals(record.getStatus());
            boolean locked = i == 0 ? false : !prevCompleted;
            TrainingLessonVO vo = new TrainingLessonVO();
            vo.setId(lesson.getId());
            vo.setLessonType(lesson.getLessonType());
            vo.setTitle(lesson.getTitle());
            vo.setSortOrder(lesson.getSortOrder());
            vo.setStatus(lesson.getStatus());
            vo.setCompleted(completed);
            vo.setLocked(locked);
            vo.setProgress(record == null ? 0 : record.getProgress());
            vo.setScore(record == null ? null : record.getScore());
            lessonVOs.add(vo);
            prevCompleted = completed;
        }

        TrainingCourseDetailVO detail = new TrainingCourseDetailVO();
        detail.setId(course.getId());
        detail.setCertificationId(course.getCertificationId());
        detail.setCertificationName(cert == null ? null : cert.getName());
        detail.setTitle(course.getTitle());
        detail.setSummary(course.getSummary());
        WorkerCertification wc = workerId == null || cert == null ? null
                : workerCertificationMapper.findByWorkerAndCert(workerId, cert.getId()).orElse(null);
        detail.setCertified(wc != null && CERT_ACTIVE.equals(wc.getStatus())
                && (wc.getExpiresAt() == null || wc.getExpiresAt().isAfter(LocalDateTime.now())));
        detail.setLessons(lessonVOs);
        return detail;
    }

    @Override
    @Transactional
    public LessonStartVO startLesson(Long workerId, StartLessonCmd cmd) {
        TrainingLesson lesson = trainingLessonMapper.findById(cmd.getLessonId())
                .orElseThrow(() -> new RuntimeException("课时不存在"));
        if (!"PUBLISHED".equals(lesson.getStatus())) {
            throw new RuntimeException("课时未发布");
        }
        List<TrainingLesson> courseLessons = trainingLessonMapper.findByCourseId(lesson.getCourseId());
        int idx = -1;
        for (int i = 0; i < courseLessons.size(); i++) {
            if (courseLessons.get(i).getId().equals(lesson.getId())) {
                idx = i;
                break;
            }
        }
        if (idx < 0) {
            throw new RuntimeException("课时未发布");
        }
        if (idx > 0) {
            TrainingLesson prev = courseLessons.get(idx - 1);
            WorkerLessonRecord prevRecord = workerLessonRecordMapper
                    .findByWorkerAndLesson(workerId, prev.getId()).orElse(null);
            if (prevRecord == null || !STATUS_COMPLETED.equals(prevRecord.getStatus())) {
                throw new RuntimeException("请先完成上一课时");
            }
        }
        if ("EXAM".equals(lesson.getLessonType())) {
            return drawExamPaper(lesson);
        }
        WorkerLessonRecord record = workerLessonRecordMapper
                .findByWorkerAndLesson(workerId, lesson.getId()).orElse(null);
        int currentProgress = 0;
        if (record == null) {
            record = new WorkerLessonRecord();
            record.setWorkerId(workerId);
            record.setLessonId(lesson.getId());
            record.setStatus(STATUS_IN_PROGRESS);
            record.setProgress(0);
            record.setExamAttempts(0);
            record.setStartedAt(LocalDateTime.now());
            workerLessonRecordMapper.insert(record);
        } else {
            currentProgress = record.getProgress() == null ? 0 : record.getProgress();
        }
        LessonStartVO vo = new LessonStartVO();
        vo.setLessonId(lesson.getId());
        vo.setLessonType(lesson.getLessonType());
        vo.setTitle(lesson.getTitle());
        vo.setContent(lesson.getContent());
        vo.setMediaUrl(lesson.getMediaUrl());
        vo.setDurationMinutes(lesson.getDurationMinutes());
        vo.setCurrentProgress(currentProgress);
        return vo;
    }

    private LessonStartVO drawExamPaper(TrainingLesson lesson) {
        throw new UnsupportedOperationException("考试抽题待 Task14 实现");
    }

    @Override
    @Transactional
    public void startCourse(Long workerId, Long courseId) {
        trainingCourseMapper.findPublishedById(courseId)
                .orElseThrow(() -> new RuntimeException("课程不存在或未发布"));
        Optional<WorkerTrainingRecord> existing = workerTrainingRecordMapper.findByWorkerAndCourse(workerId, courseId);
        if (existing.isPresent()) {
            return;
        }
        WorkerTrainingRecord record = new WorkerTrainingRecord();
        record.setWorkerId(workerId);
        record.setCourseId(courseId);
        record.setStatus(STATUS_IN_PROGRESS);
        record.setStartedAt(LocalDateTime.now());
        workerTrainingRecordMapper.insert(record);
    }

    @Override
    @Transactional
    public ExamResultVO submitExam(Long workerId, Long courseId, ExamSubmitCmd cmd) {
        TrainingCourse course = trainingCourseMapper.findPublishedById(courseId)
                .orElseThrow(() -> new RuntimeException("课程不存在或未发布"));
        List<Map<String, Object>> questions = parseQuestionMaps(course.getExamJson());
        int passScore = course.getPassScore() == null ? 60 : course.getPassScore();

        List<Integer> answers = cmd.getAnswers() == null ? List.of() : cmd.getAnswers();
        int correct = 0;
        for (int i = 0; i < questions.size(); i++) {
            if (i >= answers.size()) {
                break;
            }
            Object answerObj = questions.get(i).get("answer");
            int correctAnswer = answerObj == null ? -1 : Integer.parseInt(String.valueOf(answerObj));
            if (correctAnswer >= 0 && correctAnswer == answers.get(i)) {
                correct++;
            }
        }
        int total = questions.size();
        int score = total == 0 ? 100 : Math.round(correct * 100f / total);
        boolean passed = score >= passScore;

        WorkerTrainingRecord record = workerTrainingRecordMapper.findByWorkerAndCourse(workerId, courseId)
                .orElseGet(() -> {
                    WorkerTrainingRecord newRecord = new WorkerTrainingRecord();
                    newRecord.setWorkerId(workerId);
                    newRecord.setCourseId(courseId);
                    newRecord.setStatus(STATUS_IN_PROGRESS);
                    newRecord.setStartedAt(LocalDateTime.now());
                    workerTrainingRecordMapper.insert(newRecord);
                    return newRecord;
                });
        record.setScore(score);
        record.setStatus(passed ? STATUS_COMPLETED : STATUS_FAILED);
        record.setCompletedAt(LocalDateTime.now());
        workerTrainingRecordMapper.update(record);

        if (passed) {
            grantCertification(workerId, course.getCertificationId());
        }

        ExamResultVO result = new ExamResultVO();
        result.setScore(score);
        result.setPassScore(passScore);
        result.setPassed(passed);
        result.setMessage(passed ? "恭喜，考试通过，已获得技能认证！" : "很遗憾，未达到及格分，可重新考试。");
        return result;
    }

    @Override
    public List<WorkerCertificationVO> getMyCertifications(Long workerId) {
        List<WorkerCertification> myCerts = workerCertificationMapper.findByWorkerId(workerId);
        if (myCerts.isEmpty()) {
            return List.of();
        }
        List<Long> certIds = myCerts.stream()
                .map(WorkerCertification::getCertificationId)
                .distinct()
                .toList();
        Map<Long, TrainingCertification> certMap = trainingCertificationMapper.findByIds(certIds).stream()
                .collect(Collectors.toMap(TrainingCertification::getId, Function.identity()));
        LocalDateTime now = LocalDateTime.now();
        return myCerts.stream().map(wc -> {
            WorkerCertificationVO vo = new WorkerCertificationVO();
            vo.setCertificationId(wc.getCertificationId());
            vo.setGrantedAt(wc.getGrantedAt());
            vo.setExpiresAt(wc.getExpiresAt());
            vo.setStatus(wc.getStatus());
            if (CERT_ACTIVE.equals(wc.getStatus())
                    && wc.getExpiresAt() != null && wc.getExpiresAt().isBefore(now)) {
                vo.setStatus("EXPIRED");
            }
            TrainingCertification cert = certMap.get(wc.getCertificationId());
            if (cert != null) {
                vo.setName(cert.getName());
                vo.setCode(cert.getCode());
                vo.setTaskType(cert.getTaskType());
            }
            return vo;
        }).toList();
    }

    private void grantCertification(Long workerId, Long certificationId) {
        TrainingCertification cert = trainingCertificationMapper.findById(certificationId)
                .orElse(null);
        if (cert == null || !CERT_ACTIVE.equals(cert.getStatus())) {
            log.warn("认证不存在或已停用，无法发放 certificationId={}", certificationId);
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = cert.getValidDays() == null ? null : now.plusDays(cert.getValidDays());
        Optional<WorkerCertification> existing = workerCertificationMapper.findByWorkerAndCert(workerId, certificationId);
        if (existing.isPresent()) {
            WorkerCertification wc = existing.get();
            wc.setStatus(CERT_ACTIVE);
            wc.setGrantedAt(now);
            wc.setExpiresAt(expiresAt);
            workerCertificationMapper.update(wc);
        } else {
            WorkerCertification wc = new WorkerCertification();
            wc.setWorkerId(workerId);
            wc.setCertificationId(certificationId);
            wc.setStatus(CERT_ACTIVE);
            wc.setGrantedAt(now);
            wc.setExpiresAt(expiresAt);
            workerCertificationMapper.insert(wc);
        }
    }

    private List<ExamQuestionVO> parseQuestions(String examJson) {
        List<Map<String, Object>> maps = parseQuestionMaps(examJson);
        List<ExamQuestionVO> result = new ArrayList<>();
        for (int i = 0; i < maps.size(); i++) {
            Map<String, Object> m = maps.get(i);
            ExamQuestionVO q = new ExamQuestionVO();
            q.setIndex(i + 1);
            q.setQuestion(String.valueOf(m.getOrDefault("question", "")));
            Object optionsObj = m.get("options");
            if (optionsObj instanceof List<?> list) {
                q.setOptions(list.stream().map(String::valueOf).toList());
            }
            result.add(q);
        }
        return result;
    }

    private List<Map<String, Object>> parseQuestionMaps(String examJson) {
        if (examJson == null || examJson.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(examJson, new TypeReference<List<Map<String, Object>>>() {
            });
        } catch (Exception e) {
            log.error("解析考试题目失败 course examJson={}", examJson, e);
            return List.of();
        }
    }
}
