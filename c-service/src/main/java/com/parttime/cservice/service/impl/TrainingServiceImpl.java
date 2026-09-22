package com.parttime.cservice.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.parttime.cservice.mapper.TrainingCertificationMapper;
import com.parttime.cservice.mapper.TrainingCourseMapper;
import com.parttime.cservice.mapper.TrainingLessonMapper;
import com.parttime.cservice.mapper.WorkerCertificationMapper;
import com.parttime.cservice.mapper.WorkerLessonRecordMapper;
import com.parttime.cservice.mapper.QuestionBankMapper;
import com.parttime.cservice.mapper.QuestionBankQuestionMapper;
import com.parttime.cservice.mapper.WorkerTrainingRecordMapper;
import com.parttime.cservice.pojo.cmd.ExamSubmitCmd;
import com.parttime.cservice.pojo.cmd.LessonCompleteCmd;
import com.parttime.cservice.pojo.cmd.LessonExamAnswerItem;
import com.parttime.cservice.pojo.cmd.LessonExamSubmitCmd;
import com.parttime.cservice.pojo.cmd.LessonProgressCmd;
import com.parttime.cservice.pojo.cmd.StartLessonCmd;
import com.parttime.cservice.pojo.entity.TrainingCertification;
import com.parttime.cservice.pojo.entity.TrainingCourse;
import com.parttime.cservice.pojo.entity.TrainingLesson;
import com.parttime.cservice.pojo.entity.WorkerCertification;
import com.parttime.cservice.pojo.entity.WorkerLessonRecord;
import com.parttime.cservice.pojo.entity.QuestionBank;
import com.parttime.cservice.pojo.entity.QuestionBankQuestion;
import com.parttime.cservice.pojo.entity.WorkerTrainingRecord;
import com.parttime.cservice.pojo.vo.ExamQuestionVO;
import com.parttime.cservice.pojo.vo.ExamOptionVO;
import com.parttime.cservice.pojo.vo.ExamPaperQuestionVO;
import com.parttime.cservice.pojo.vo.ExamPaperVO;
import com.parttime.cservice.pojo.vo.ExamResultVO;
import com.parttime.cservice.pojo.vo.LessonExamResultVO;
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
    private QuestionBankMapper questionBankMapper;

    @Resource
    private QuestionBankQuestionMapper questionBankQuestionMapper;

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
            LessonStartVO examVo = new LessonStartVO();
            examVo.setLessonId(lesson.getId());
            examVo.setLessonType(lesson.getLessonType());
            examVo.setTitle(lesson.getTitle());
            examVo.setPaper(drawExamPaper(lesson));
            return examVo;
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

    @Override
    @Transactional
    public void reportProgress(Long workerId, LessonProgressCmd cmd) {
        TrainingLesson lesson = trainingLessonMapper.findById(cmd.getLessonId())
                .orElseThrow(() -> new RuntimeException("课时不存在"));
        if (!"PUBLISHED".equals(lesson.getStatus())) {
            throw new RuntimeException("课时未发布");
        }
        if (!"VIDEO".equals(lesson.getLessonType()) && !"AUDIO".equals(lesson.getLessonType())) {
            throw new RuntimeException("仅音视频课时可上报进度");
        }
        WorkerLessonRecord record = workerLessonRecordMapper
                .findByWorkerAndLesson(workerId, lesson.getId()).orElse(null);
        if (record == null) {
            throw new RuntimeException("请先开始课时");
        }
        int newProgress = cmd.getProgress() == null ? 0 : cmd.getProgress();
        if (newProgress > 100) {
            newProgress = 100;
        }
        int oldProgress = record.getProgress() == null ? 0 : record.getProgress();
        if (newProgress < oldProgress) {
            throw new RuntimeException("进度不能回退");
        }
        record.setProgress(newProgress);
        boolean autoComplete = newProgress >= 100 && !STATUS_COMPLETED.equals(record.getStatus());
        if (autoComplete) {
            record.setStatus(STATUS_COMPLETED);
            record.setCompletedAt(LocalDateTime.now());
        }
        workerLessonRecordMapper.update(record);
        if (autoComplete) {
            checkAndGrantCertification(lesson.getCourseId());
        }
    }

    @Override
    @Transactional
    public void markComplete(Long workerId, LessonCompleteCmd cmd) {
        TrainingLesson lesson = trainingLessonMapper.findById(cmd.getLessonId())
                .orElseThrow(() -> new RuntimeException("课时不存在"));
        if (!"PUBLISHED".equals(lesson.getStatus())) {
            throw new RuntimeException("课时未发布");
        }
        if (!"DOCUMENT".equals(lesson.getLessonType()) && !"IMAGE_TEXT".equals(lesson.getLessonType())) {
            throw new RuntimeException("仅文档/图文课时可标记已读");
        }
        WorkerLessonRecord record = workerLessonRecordMapper
                .findByWorkerAndLesson(workerId, lesson.getId()).orElse(null);
        if (record == null) {
            record = new WorkerLessonRecord();
            record.setWorkerId(workerId);
            record.setLessonId(lesson.getId());
            record.setStatus(STATUS_IN_PROGRESS);
            record.setProgress(0);
            record.setExamAttempts(0);
            record.setStartedAt(LocalDateTime.now());
            workerLessonRecordMapper.insert(record);
        }
        if (!STATUS_COMPLETED.equals(record.getStatus())) {
            record.setStatus(STATUS_COMPLETED);
            record.setProgress(100);
            record.setCompletedAt(LocalDateTime.now());
            workerLessonRecordMapper.update(record);
            checkAndGrantCertification(lesson.getCourseId());
        }
    }

    @Override
    @Transactional
    public LessonExamResultVO submitLessonExam(Long workerId, LessonExamSubmitCmd cmd) {
        TrainingLesson lesson = trainingLessonMapper.findById(cmd.getLessonId())
                .orElseThrow(() -> new RuntimeException("课时不存在"));
        if (!"PUBLISHED".equals(lesson.getStatus())) {
            throw new RuntimeException("课时未发布");
        }
        if (!"EXAM".equals(lesson.getLessonType())) {
            throw new RuntimeException("仅考试课时可提交考试");
        }
        Map<String, Object> cfg;
        try {
            cfg = objectMapper.readValue(lesson.getExamConfigJson(), new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            throw new RuntimeException("考试配置解析失败");
        }
        if (cfg == null || cfg.get("bankId") == null) {
            throw new RuntimeException("考试配置缺失");
        }
        Long bankId = ((Number) cfg.get("bankId")).longValue();
        int passScore = cfg.get("passScore") == null ? 0 : ((Number) cfg.get("passScore")).intValue();

        WorkerLessonRecord record = workerLessonRecordMapper
                .findByWorkerAndLesson(workerId, lesson.getId()).orElse(null);

        if (record != null && STATUS_COMPLETED.equals(record.getStatus())) {
            LessonExamResultVO done = new LessonExamResultVO();
            done.setPassed(true);
            done.setScore(record.getScore());
            done.setPassScore(passScore);
            done.setTotalScore(computeConfiguredTotal(cfg));
            done.setSnapshot(parseJsonOrNull(record.getExamSnapshotJson()));
            return done;
        }

        Object rulesObj = cfg.get("rules");
        if (!(rulesObj instanceof List<?> rawRules)) {
            throw new RuntimeException("考试规则缺失");
        }
        Map<String, Integer> scorePerByType = new LinkedHashMap<>();
        int configuredTotal = 0;
        for (Object ro : rawRules) {
            Map<?, ?> rule = (Map<?, ?>) ro;
            String qt = String.valueOf(rule.get("questionType"));
            int count = ((Number) rule.get("count")).intValue();
            int sp = ((Number) rule.get("scorePer")).intValue();
            scorePerByType.put(qt, sp);
            configuredTotal += count * sp;
        }

        List<LessonExamAnswerItem> answers = cmd.getAnswers() == null ? List.of() : cmd.getAnswers();
        int earned = 0;
        List<Map<String, Object>> reviewQuestions = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (LessonExamAnswerItem ai : answers) {
            QuestionBankQuestion q = questionBankQuestionMapper.findById(ai.getQuestionId()).orElse(null);
            if (q == null || !bankId.equals(q.getBankId())) {
                throw new RuntimeException("题目不存在或不属于本考试题库");
            }
            int scorePer = scorePerByType.getOrDefault(q.getQuestionType(), 0);
            String myAnswer = ai.getAnswer() == null ? "" : ai.getAnswer().trim();
            String correctAnswer = q.getAnswer() == null ? "" : q.getAnswer().trim();
            boolean correct;
            if ("MULTIPLE_CHOICE".equals(q.getQuestionType())) {
                correct = keySetOf(myAnswer).equals(keySetOf(correctAnswer));
            } else {
                correct = myAnswer.equals(correctAnswer);
            }
            if (correct) {
                earned += scorePer;
            }
            Map<String, Object> rq = new LinkedHashMap<>();
            rq.put("questionId", q.getId());
            rq.put("stem", q.getStem());
            rq.put("options", parseExamOptions(q.getOptionsJson()));
            rq.put("myAnswer", ai.getAnswer());
            rq.put("correctAnswer", q.getAnswer());
            rq.put("score", correct ? scorePer : 0);
            rq.put("correct", correct);
            reviewQuestions.add(rq);
        }
        boolean passed = earned >= passScore;

        int attempts = (record == null || record.getExamAttempts() == null ? 0 : record.getExamAttempts()) + 1;
        if (record == null) {
            record = new WorkerLessonRecord();
            record.setWorkerId(workerId);
            record.setLessonId(lesson.getId());
            record.setStartedAt(now);
        }
        record.setExamAttempts(attempts);
        record.setScore(earned);
        String snapshotJson = null;
        if (passed) {
            record.setStatus(STATUS_COMPLETED);
            record.setCompletedAt(now);
            Map<String, Object> snapshot = new LinkedHashMap<>();
            snapshot.put("submittedAt", now.toString());
            snapshot.put("score", earned);
            snapshot.put("passScore", passScore);
            snapshot.put("totalScore", configuredTotal);
            snapshot.put("questions", reviewQuestions);
            try {
                snapshotJson = objectMapper.writeValueAsString(snapshot);
            } catch (Exception e) {
                throw new RuntimeException("考试快照生成失败");
            }
            record.setExamSnapshotJson(snapshotJson);
        } else {
            record.setStatus(STATUS_FAILED);
            record.setExamSnapshotJson(null);
        }
        workerLessonRecordMapper.updateExamResult(record);

        if (passed) {
            checkAndGrantCertification(lesson.getCourseId());
        }

        LessonExamResultVO vo = new LessonExamResultVO();
        vo.setPassed(passed);
        vo.setScore(earned);
        vo.setPassScore(passScore);
        vo.setTotalScore(configuredTotal);
        vo.setSnapshot(passed ? parseJsonOrNull(snapshotJson) : null);
        return vo;
    }

    private int computeConfiguredTotal(Map<String, Object> cfg) {
        Object rulesObj = cfg.get("rules");
        if (!(rulesObj instanceof List<?> rawRules)) {
            return 0;
        }
        int total = 0;
        for (Object ro : rawRules) {
            Map<?, ?> rule = (Map<?, ?>) ro;
            int count = ((Number) rule.get("count")).intValue();
            int sp = ((Number) rule.get("scorePer")).intValue();
            total += count * sp;
        }
        return total;
    }

    private java.util.Set<String> keySetOf(String jsonArray) {
        if (jsonArray == null || jsonArray.isBlank()) {
            return java.util.Collections.emptySet();
        }
        try {
            List<String> list = objectMapper.readValue(jsonArray, new TypeReference<List<String>>() {
            });
            return new java.util.TreeSet<>(list);
        } catch (Exception e) {
            return java.util.Collections.emptySet();
        }
    }

    private Object parseJsonOrNull(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, Object.class);
        } catch (Exception e) {
            return null;
        }
    }

    private void checkAndGrantCertification(Long courseId) {
        log.info("课程完成检查 courseId={}，待 Task16 实现发认证逻辑", courseId);
    }

    private ExamPaperVO drawExamPaper(TrainingLesson lesson) {
        Map<String, Object> cfg;
        try {
            cfg = objectMapper.readValue(lesson.getExamConfigJson(), new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            throw new RuntimeException("考试配置解析失败");
        }
        if (cfg == null || cfg.get("bankId") == null) {
            throw new RuntimeException("考试配置缺失");
        }
        Long bankId = ((Number) cfg.get("bankId")).longValue();
        QuestionBank bank = questionBankMapper.findById(bankId).orElse(null);
        if (bank == null || !"ACTIVE".equals(bank.getStatus())) {
            throw new RuntimeException("题库不存在或已停用，无法开考");
        }
        Object rulesObj = cfg.get("rules");
        if (!(rulesObj instanceof List<?> rawRules) || rawRules.isEmpty()) {
            throw new RuntimeException("考试规则缺失");
        }
        ExamPaperVO paper = new ExamPaperVO();
        paper.setLessonId(lesson.getId());
        Object durationObj = cfg.get("durationMinutes");
        paper.setDurationMinutes(durationObj == null ? null : ((Number) durationObj).intValue());
        Object passScoreObj = cfg.get("passScore");
        paper.setPassScore(passScoreObj == null ? null : ((Number) passScoreObj).intValue());

        int totalScore = 0;
        List<ExamPaperQuestionVO> questions = new ArrayList<>();
        for (Object ruleObj : rawRules) {
            Map<?, ?> rule = (Map<?, ?>) ruleObj;
            String questionType = String.valueOf(rule.get("questionType"));
            int count = ((Number) rule.get("count")).intValue();
            int scorePer = ((Number) rule.get("scorePer")).intValue();
            int available = questionBankQuestionMapper.countPublishedByBankAndType(bankId, questionType);
            if (available < count) {
                throw new RuntimeException("题库题量不足，请联系平台配置");
            }
            List<QuestionBankQuestion> picked = questionBankQuestionMapper
                    .randomPublishedByBankAndType(bankId, questionType, count);
            totalScore += count * scorePer;
            for (QuestionBankQuestion q : picked) {
                ExamPaperQuestionVO vo = new ExamPaperQuestionVO();
                vo.setQuestionId(q.getId());
                vo.setQuestionType(q.getQuestionType());
                vo.setStem(q.getStem());
                vo.setOptions(parseExamOptions(q.getOptionsJson()));
                vo.setScore(scorePer);
                questions.add(vo);
            }
        }
        paper.setTotalScore(totalScore);
        paper.setQuestions(questions);
        return paper;
    }

    private List<ExamOptionVO> parseExamOptions(String optionsJson) {
        if (optionsJson == null || optionsJson.isBlank()) {
            return List.of();
        }
        try {
            List<Map<String, Object>> list = objectMapper.readValue(optionsJson,
                    new TypeReference<List<Map<String, Object>>>() {
                    });
            List<ExamOptionVO> result = new ArrayList<>();
            for (Map<String, Object> m : list) {
                ExamOptionVO opt = new ExamOptionVO();
                opt.setKey(String.valueOf(m.get("key")));
                opt.setLabel(String.valueOf(m.get("label")));
                result.add(opt);
            }
            return result;
        } catch (Exception e) {
            log.error("解析考试选项失败 optionsJson={}", optionsJson, e);
            return List.of();
        }
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
