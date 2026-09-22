package com.parttime.cservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parttime.cservice.mapper.QuestionBankMapper;
import com.parttime.cservice.mapper.QuestionBankQuestionMapper;
import com.parttime.cservice.mapper.TrainingCertificationMapper;
import com.parttime.cservice.mapper.TrainingCourseMapper;
import com.parttime.cservice.mapper.TrainingLessonMapper;
import com.parttime.cservice.mapper.WorkerCertificationMapper;
import com.parttime.cservice.mapper.WorkerLessonRecordMapper;
import com.parttime.cservice.mapper.WorkerTrainingRecordMapper;
import com.parttime.cservice.pojo.cmd.LessonCompleteCmd;
import com.parttime.cservice.pojo.cmd.LessonExamAnswerItem;
import com.parttime.cservice.pojo.cmd.LessonExamSubmitCmd;
import com.parttime.cservice.pojo.cmd.LessonProgressCmd;
import com.parttime.cservice.pojo.cmd.StartLessonCmd;
import com.parttime.cservice.pojo.entity.QuestionBank;
import com.parttime.cservice.pojo.entity.QuestionBankQuestion;
import com.parttime.cservice.pojo.entity.TrainingCertification;
import com.parttime.cservice.pojo.entity.TrainingCourse;
import com.parttime.cservice.pojo.entity.TrainingLesson;
import com.parttime.cservice.pojo.entity.WorkerCertification;
import com.parttime.cservice.pojo.entity.WorkerLessonRecord;
import com.parttime.cservice.pojo.vo.LessonExamResultVO;
import com.parttime.cservice.pojo.vo.LessonStartVO;
import com.parttime.cservice.pojo.vo.TrainingCourseDetailVO;
import com.parttime.cservice.service.impl.TrainingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingV2ServiceTest {

    private static final Long WORKER = 100L;
    private static final Long COURSE = 1L;

    @Mock
    private TrainingCourseMapper trainingCourseMapper;
    @Mock
    private TrainingCertificationMapper trainingCertificationMapper;
    @Mock
    private TrainingLessonMapper trainingLessonMapper;
    @Mock
    private WorkerTrainingRecordMapper workerTrainingRecordMapper;
    @Mock
    private WorkerLessonRecordMapper workerLessonRecordMapper;
    @Mock
    private WorkerCertificationMapper workerCertificationMapper;
    @Mock
    private QuestionBankMapper questionBankMapper;
    @Mock
    private QuestionBankQuestionMapper questionBankQuestionMapper;

    @InjectMocks
    private TrainingServiceImpl trainingService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws Exception {
        java.lang.reflect.Field field = TrainingServiceImpl.class.getDeclaredField("objectMapper");
        field.setAccessible(true);
        field.set(trainingService, objectMapper);
    }

    private TrainingCourse course(Long certId) {
        TrainingCourse c = new TrainingCourse();
        c.setId(COURSE);
        c.setCertificationId(certId);
        c.setTitle("标注入门");
        c.setSummary("摘要");
        c.setStatus("PUBLISHED");
        return c;
    }

    private TrainingLesson lesson(Long id, String type, int sort) {
        TrainingLesson l = new TrainingLesson();
        l.setId(id);
        l.setCourseId(COURSE);
        l.setLessonType(type);
        l.setTitle("课时" + id);
        l.setSortOrder(sort);
        l.setStatus("PUBLISHED");
        l.setContent("正文" + id);
        l.setMediaUrl("http://media/" + id);
        l.setDurationMinutes(5);
        return l;
    }

    private WorkerLessonRecord record(Long lessonId, String status, Integer progress) {
        WorkerLessonRecord r = new WorkerLessonRecord();
        r.setId(lessonId * 10);
        r.setWorkerId(WORKER);
        r.setLessonId(lessonId);
        r.setStatus(status);
        r.setProgress(progress);
        r.setExamAttempts(0);
        return r;
    }

    private TrainingCertification activeCert() {
        TrainingCertification c = new TrainingCertification();
        c.setId(1L);
        c.setName("标注认证");
        c.setStatus("ACTIVE");
        c.setValidDays(30);
        return c;
    }

    private QuestionBank activeBank() {
        QuestionBank b = new QuestionBank();
        b.setId(1L);
        b.setStatus("ACTIVE");
        return b;
    }

    private QuestionBankQuestion question(Long id, String type, String stem, String optionsJson, String answer) {
        QuestionBankQuestion q = new QuestionBankQuestion();
        q.setId(id);
        q.setBankId(1L);
        q.setQuestionType(type);
        q.setStem(stem);
        q.setOptionsJson(optionsJson);
        q.setAnswer(answer);
        return q;
    }

    private String examConfig(int singleCount, int judgeCount) {
        return "{\"bankId\":1,\"durationMinutes\":30,\"passScore\":15,"
                + "\"rules\":["
                + "{\"questionType\":\"SINGLE_CHOICE\",\"count\":" + singleCount + ",\"scorePer\":10},"
                + "{\"questionType\":\"JUDGE\",\"count\":" + judgeCount + ",\"scorePer\":5}"
                + "]}";
    }

    // ---------- 1. 顺序锁定 ----------

    @Test
    void detail_firstLessonUnlocked_laterLockedWhenPrevNotCompleted() {
        when(trainingCourseMapper.findPublishedById(COURSE)).thenReturn(Optional.of(course(1L)));
        when(trainingCertificationMapper.findById(1L)).thenReturn(Optional.empty());
        TrainingLesson l1 = lesson(11L, "VIDEO", 1);
        TrainingLesson l2 = lesson(12L, "VIDEO", 2);
        TrainingLesson l3 = lesson(13L, "DOCUMENT", 3);
        when(trainingLessonMapper.findByCourseId(COURSE)).thenReturn(List.of(l1, l2, l3));
        when(workerLessonRecordMapper.findByWorkerId(WORKER)).thenReturn(List.of());

        TrainingCourseDetailVO vo = trainingService.getCourseDetail(WORKER, COURSE);

        assertThat(vo.getLessons()).hasSize(3);
        assertThat(vo.getLessons().get(0).getLocked()).isFalse();
        assertThat(vo.getLessons().get(1).getLocked()).isTrue();
        assertThat(vo.getLessons().get(2).getLocked()).isTrue();
        assertThat(vo.getLessons().get(0).getCompleted()).isFalse();
    }

    @Test
    void detail_secondLessonUnlockedWhenFirstCompleted() {
        when(trainingCourseMapper.findPublishedById(COURSE)).thenReturn(Optional.of(course(1L)));
        when(trainingCertificationMapper.findById(1L)).thenReturn(Optional.empty());
        when(trainingLessonMapper.findByCourseId(COURSE)).thenReturn(List.of(lesson(11L, "VIDEO", 1), lesson(12L, "VIDEO", 2)));
        when(workerLessonRecordMapper.findByWorkerId(WORKER))
                .thenReturn(List.of(record(11L, "COMPLETED", 100)));

        TrainingCourseDetailVO vo = trainingService.getCourseDetail(WORKER, COURSE);

        assertThat(vo.getLessons().get(0).getLocked()).isFalse();
        assertThat(vo.getLessons().get(1).getLocked()).isFalse();
    }

    // ---------- 2. startLesson ----------

    @Test
    void startLesson_notPublished_throws() {
        TrainingLesson l = lesson(11L, "VIDEO", 1);
        l.setStatus("DRAFT");
        when(trainingLessonMapper.findById(11L)).thenReturn(Optional.of(l));

        StartLessonCmd cmd = new StartLessonCmd();
        cmd.setLessonId(11L);
        assertThatThrownBy(() -> trainingService.startLesson(WORKER, cmd))
                .isInstanceOf(RuntimeException.class).hasMessageContaining("课时未发布");
    }

    @Test
    void startLesson_prevNotCompleted_throws() {
        when(trainingLessonMapper.findById(12L)).thenReturn(Optional.of(lesson(12L, "VIDEO", 2)));
        when(trainingLessonMapper.findByCourseId(COURSE))
                .thenReturn(List.of(lesson(11L, "VIDEO", 1), lesson(12L, "VIDEO", 2)));
        when(workerLessonRecordMapper.findByWorkerAndLesson(WORKER, 11L)).thenReturn(Optional.empty());

        StartLessonCmd cmd = new StartLessonCmd();
        cmd.setLessonId(12L);
        assertThatThrownBy(() -> trainingService.startLesson(WORKER, cmd))
                .isInstanceOf(RuntimeException.class).hasMessageContaining("请先完成上一课时");
    }

    @Test
    void startLesson_learning_returnsContentAndCreatesInProgress() {
        when(trainingLessonMapper.findById(11L)).thenReturn(Optional.of(lesson(11L, "VIDEO", 1)));
        when(trainingLessonMapper.findByCourseId(COURSE)).thenReturn(List.of(lesson(11L, "VIDEO", 1)));
        when(workerLessonRecordMapper.findByWorkerAndLesson(WORKER, 11L)).thenReturn(Optional.empty());

        StartLessonCmd cmd = new StartLessonCmd();
        cmd.setLessonId(11L);
        LessonStartVO vo = trainingService.startLesson(WORKER, cmd);

        assertThat(vo.getMediaUrl()).isEqualTo("http://media/11");
        assertThat(vo.getContent()).isEqualTo("正文11");
        assertThat(vo.getCurrentProgress()).isZero();
        verify(workerLessonRecordMapper).insert(any(WorkerLessonRecord.class));
    }

    // ---------- 3. reportProgress ----------

    @Test
    void reportProgress_nonVideoAudio_throws() {
        TrainingLesson l = lesson(11L, "DOCUMENT", 1);
        when(trainingLessonMapper.findById(11L)).thenReturn(Optional.of(l));
        LessonProgressCmd cmd = new LessonProgressCmd();
        cmd.setLessonId(11L);
        cmd.setProgress(50);
        assertThatThrownBy(() -> trainingService.reportProgress(WORKER, cmd))
                .isInstanceOf(RuntimeException.class).hasMessageContaining("仅音视频");
    }

    @Test
    void reportProgress_noRecord_throws() {
        when(trainingLessonMapper.findById(11L)).thenReturn(Optional.of(lesson(11L, "VIDEO", 1)));
        when(workerLessonRecordMapper.findByWorkerAndLesson(WORKER, 11L)).thenReturn(Optional.empty());
        LessonProgressCmd cmd = new LessonProgressCmd();
        cmd.setLessonId(11L);
        cmd.setProgress(50);
        assertThatThrownBy(() -> trainingService.reportProgress(WORKER, cmd))
                .isInstanceOf(RuntimeException.class).hasMessageContaining("请先开始课时");
    }

    @Test
    void reportProgress_regression_throws() {
        when(trainingLessonMapper.findById(11L)).thenReturn(Optional.of(lesson(11L, "VIDEO", 1)));
        when(workerLessonRecordMapper.findByWorkerAndLesson(WORKER, 11L))
                .thenReturn(Optional.of(record(11L, "IN_PROGRESS", 80)));
        LessonProgressCmd cmd = new LessonProgressCmd();
        cmd.setLessonId(11L);
        cmd.setProgress(30);
        assertThatThrownBy(() -> trainingService.reportProgress(WORKER, cmd))
                .isInstanceOf(RuntimeException.class).hasMessageContaining("进度不能回退");
    }

    @Test
    void reportProgress_reach100_marksCompleted() {
        when(trainingLessonMapper.findById(11L)).thenReturn(Optional.of(lesson(11L, "VIDEO", 1)));
        when(workerLessonRecordMapper.findByWorkerAndLesson(WORKER, 11L))
                .thenReturn(Optional.of(record(11L, "IN_PROGRESS", 80)));
        LessonProgressCmd cmd = new LessonProgressCmd();
        cmd.setLessonId(11L);
        cmd.setProgress(100);
        trainingService.reportProgress(WORKER, cmd);
        verify(workerLessonRecordMapper).update(any(WorkerLessonRecord.class));
    }

    // ---------- 4. markComplete ----------

    @Test
    void markComplete_document_completed() {
        when(trainingLessonMapper.findById(13L)).thenReturn(Optional.of(lesson(13L, "DOCUMENT", 3)));
        when(workerLessonRecordMapper.findByWorkerAndLesson(WORKER, 13L))
                .thenReturn(Optional.of(record(13L, "IN_PROGRESS", 0)));
        LessonCompleteCmd cmd = new LessonCompleteCmd();
        cmd.setLessonId(13L);
        trainingService.markComplete(WORKER, cmd);
        verify(workerLessonRecordMapper).update(any(WorkerLessonRecord.class));
    }

    @Test
    void markComplete_nonDocument_throws() {
        when(trainingLessonMapper.findById(11L)).thenReturn(Optional.of(lesson(11L, "VIDEO", 1)));
        LessonCompleteCmd cmd = new LessonCompleteCmd();
        cmd.setLessonId(11L);
        assertThatThrownBy(() -> trainingService.markComplete(WORKER, cmd))
                .isInstanceOf(RuntimeException.class).hasMessageContaining("仅文档/图文");
    }

    // ---------- 5. drawExamPaper ----------

    private TrainingLesson examLesson(Long id, String config) {
        TrainingLesson l = lesson(id, "EXAM", 1);
        l.setExamConfigJson(config);
        return l;
    }

    @Test
    void startLesson_exam_insufficientBank_throws() {
        when(trainingLessonMapper.findById(20L)).thenReturn(Optional.of(examLesson(20L, examConfig(5, 0))));
        when(trainingLessonMapper.findByCourseId(COURSE)).thenReturn(List.of(examLesson(20L, examConfig(5, 0))));
        when(questionBankMapper.findById(1L)).thenReturn(Optional.of(activeBank()));
        when(questionBankQuestionMapper.countPublishedByBankAndType(1L, "SINGLE_CHOICE")).thenReturn(2);

        StartLessonCmd cmd = new StartLessonCmd();
        cmd.setLessonId(20L);
        assertThatThrownBy(() -> trainingService.startLesson(WORKER, cmd))
                .isInstanceOf(RuntimeException.class).hasMessageContaining("题量不足");
    }

    @Test
    void startLesson_exam_returnsPaperWithoutAnswers() {
        TrainingLesson ex = examLesson(20L, examConfig(2, 0));
        when(trainingLessonMapper.findById(20L)).thenReturn(Optional.of(ex));
        when(trainingLessonMapper.findByCourseId(COURSE)).thenReturn(List.of(ex));
        when(questionBankMapper.findById(1L)).thenReturn(Optional.of(activeBank()));
        when(questionBankQuestionMapper.countPublishedByBankAndType(1L, "SINGLE_CHOICE")).thenReturn(2);
        when(questionBankQuestionMapper.randomPublishedByBankAndType(1L, "SINGLE_CHOICE", 2))
                .thenReturn(List.of(
                        question(100L, "SINGLE_CHOICE", "题1", "[{\"key\":\"A\",\"label\":\"a\"},{\"key\":\"B\",\"label\":\"b\"}]", "A"),
                        question(101L, "SINGLE_CHOICE", "题2", "[{\"key\":\"A\",\"label\":\"a\"}]", "A")));

        StartLessonCmd cmd = new StartLessonCmd();
        cmd.setLessonId(20L);
        LessonStartVO vo = trainingService.startLesson(WORKER, cmd);

        assertThat(vo.getPaper()).isNotNull();
        assertThat(vo.getPaper().getQuestions()).hasSize(2);
        assertThat(vo.getPaper().getTotalScore()).isEqualTo(20);
        assertThat(vo.getPaper().getQuestions().get(0).getClass().getDeclaredFields())
                .extracting(java.lang.reflect.Field::getName)
                .doesNotContain("answer", "correctAnswer");
    }

    // ---------- 6. submitLessonExam 判分 ----------

    private LessonExamSubmitCmd submit(Long lessonId, LessonExamAnswerItem... items) {
        LessonExamSubmitCmd cmd = new LessonExamSubmitCmd();
        cmd.setLessonId(lessonId);
        cmd.setAnswers(List.of(items));
        return cmd;
    }

    private LessonExamAnswerItem ans(Long qid, String answer) {
        LessonExamAnswerItem a = new LessonExamAnswerItem();
        a.setQuestionId(qid);
        a.setAnswer(answer);
        return a;
    }

    private void stubExamLessonAndQuestions(String config) {
        when(trainingLessonMapper.findById(20L)).thenReturn(Optional.of(examLesson(20L, config)));
        when(questionBankQuestionMapper.findById(100L)).thenReturn(Optional.of(
                question(100L, "SINGLE_CHOICE", "题1", "[{\"key\":\"A\",\"label\":\"a\"},{\"key\":\"B\",\"label\":\"b\"}]", "A")));
        when(questionBankQuestionMapper.findById(101L)).thenReturn(Optional.of(
                question(101L, "SINGLE_CHOICE", "题2", "[{\"key\":\"A\",\"label\":\"a\"},{\"key\":\"B\",\"label\":\"b\"}]", "B")));
    }

    @Test
    void submitExam_singleChoice_correctGetsFullScore_wrongZero() {
        stubExamLessonAndQuestions(examConfig(2, 0));
        when(workerLessonRecordMapper.findByWorkerAndLesson(WORKER, 20L)).thenReturn(Optional.empty());

        LessonExamResultVO pass = trainingService.submitLessonExam(WORKER,
                submit(20L, ans(100L, "A"), ans(101L, "B")));
        assertThat(pass.getScore()).isEqualTo(20);
        assertThat(pass.getPassed()).isTrue();
        assertThat(pass.getSnapshot()).isNotNull();

        LessonExamResultVO fail = trainingService.submitLessonExam(WORKER,
                submit(20L, ans(100L, "B"), ans(101L, "A")));
        assertThat(fail.getScore()).isZero();
        assertThat(fail.getPassed()).isFalse();
        assertThat(fail.getSnapshot()).isNull();
    }

    @Test
    void submitExam_multipleChoice_strictSetEquality() {
        String config = "{\"bankId\":1,\"passScore\":10,\"rules\":[{\"questionType\":\"MULTIPLE_CHOICE\",\"count\":1,\"scorePer\":10}]}";
        when(trainingLessonMapper.findById(21L)).thenReturn(Optional.of(examLesson(21L, config)));
        when(questionBankQuestionMapper.findById(200L)).thenReturn(Optional.of(
                question(200L, "MULTIPLE_CHOICE", "多选", "[{\"key\":\"A\",\"label\":\"a\"},{\"key\":\"B\",\"label\":\"b\"}]", "[\"A\",\"B\"]")));
        when(workerLessonRecordMapper.findByWorkerAndLesson(WORKER, 21L)).thenReturn(Optional.empty());

        LessonExamResultVO full = trainingService.submitLessonExam(WORKER, submit(21L, ans(200L, "[\"A\",\"B\"]")));
        assertThat(full.getScore()).isEqualTo(10);
        assertThat(full.getPassed()).isTrue();

        when(workerLessonRecordMapper.findByWorkerAndLesson(WORKER, 21L)).thenReturn(Optional.empty());
        LessonExamResultVO partial = trainingService.submitLessonExam(WORKER, submit(21L, ans(200L, "[\"A\"]")));
        assertThat(partial.getScore()).isZero();
        assertThat(partial.getPassed()).isFalse();
    }

    @Test
    void submitExam_failed_noSnapshot_attemptsIncremented() {
        stubExamLessonAndQuestions(examConfig(2, 0));
        WorkerLessonRecord existing = record(20L, "FAILED", 0);
        existing.setExamAttempts(1);
        when(workerLessonRecordMapper.findByWorkerAndLesson(WORKER, 20L)).thenReturn(Optional.of(existing));

        LessonExamResultVO r = trainingService.submitLessonExam(WORKER,
                submit(20L, ans(100L, "B"), ans(101L, "A")));

        assertThat(r.getPassed()).isFalse();
        assertThat(existing.getExamSnapshotJson()).isNull();
        assertThat(existing.getExamAttempts()).isEqualTo(2);
        verify(workerLessonRecordMapper).updateExamResult(existing);
    }

    @Test
    void submitExam_alreadyCompleted_returnsStoredSnapshotWithoutOverwrite() {
        when(trainingLessonMapper.findById(20L)).thenReturn(Optional.of(examLesson(20L, examConfig(2, 0))));
        WorkerLessonRecord done = record(20L, "COMPLETED", 100);
        done.setScore(20);
        done.setExamSnapshotJson("{\"submittedAt\":\"2024-01-01\",\"score\":20}");
        when(workerLessonRecordMapper.findByWorkerAndLesson(WORKER, 20L)).thenReturn(Optional.of(done));

        LessonExamResultVO r = trainingService.submitLessonExam(WORKER,
                submit(20L, ans(100L, "B"), ans(101L, "A")));

        assertThat(r.getPassed()).isTrue();
        assertThat(r.getSnapshot()).isNotNull();
        verify(workerLessonRecordMapper, never()).updateExamResult(any());
    }

    @Test
    void firstSubmit_persistsViaInsertThenUpdate() {
        stubExamLessonAndQuestions(examConfig(2, 0));
        when(workerLessonRecordMapper.findByWorkerAndLesson(WORKER, 20L)).thenReturn(Optional.empty());

        LessonExamResultVO r = trainingService.submitLessonExam(WORKER,
                submit(20L, ans(100L, "A"), ans(101L, "B")));

        assertThat(r.getPassed()).isTrue();
        assertThat(r.getScore()).isEqualTo(20);
        verify(workerLessonRecordMapper).insert(any(WorkerLessonRecord.class));
        ArgumentCaptor<WorkerLessonRecord> cap = ArgumentCaptor.forClass(WorkerLessonRecord.class);
        verify(workerLessonRecordMapper).updateExamResult(cap.capture());
        WorkerLessonRecord saved = cap.getValue();
        assertThat(saved.getStatus()).isEqualTo("COMPLETED");
        assertThat(saved.getScore()).isEqualTo(20);
        assertThat(saved.getExamAttempts()).isEqualTo(1);
        assertThat(saved.getExamSnapshotJson()).isNotNull();
    }

    @Test
    void firstSubmitFailed_statusFailedAttemptsOne() {
        stubExamLessonAndQuestions(examConfig(2, 0));
        when(workerLessonRecordMapper.findByWorkerAndLesson(WORKER, 20L)).thenReturn(Optional.empty());

        LessonExamResultVO r = trainingService.submitLessonExam(WORKER,
                submit(20L, ans(100L, "B"), ans(101L, "A")));

        assertThat(r.getPassed()).isFalse();
        verify(workerLessonRecordMapper).insert(any(WorkerLessonRecord.class));
        ArgumentCaptor<WorkerLessonRecord> cap = ArgumentCaptor.forClass(WorkerLessonRecord.class);
        verify(workerLessonRecordMapper).updateExamResult(cap.capture());
        WorkerLessonRecord saved = cap.getValue();
        assertThat(saved.getStatus()).isEqualTo("FAILED");
        assertThat(saved.getExamAttempts()).isEqualTo(1);
        assertThat(saved.getExamSnapshotJson()).isNull();
    }

    @Test
    void secondSubmit_existingRecord_incrementsAttemptsToTwo() {
        stubExamLessonAndQuestions(examConfig(2, 0));
        WorkerLessonRecord existing = record(20L, "FAILED", 0);
        existing.setExamAttempts(1);
        when(workerLessonRecordMapper.findByWorkerAndLesson(WORKER, 20L)).thenReturn(Optional.of(existing));

        LessonExamResultVO r = trainingService.submitLessonExam(WORKER,
                submit(20L, ans(100L, "A"), ans(101L, "B")));

        assertThat(r.getPassed()).isTrue();
        verify(workerLessonRecordMapper, never()).insert(any());
        assertThat(existing.getExamAttempts()).isEqualTo(2);
    }

    // ---------- 7. checkAndGrantCertification ----------

    @Test
    void courseAllLessonsCompleted_grantsCertification() {
        TrainingLesson l1 = lesson(11L, "DOCUMENT", 1);
        TrainingLesson l2 = lesson(12L, "DOCUMENT", 2);
        when(trainingLessonMapper.findById(12L)).thenReturn(Optional.of(l2));
        when(workerLessonRecordMapper.findByWorkerAndLesson(WORKER, 12L))
                .thenReturn(Optional.of(record(12L, "IN_PROGRESS", 0)));
        when(trainingLessonMapper.findByCourseId(COURSE)).thenReturn(List.of(l1, l2));
        when(workerLessonRecordMapper.findByWorkerAndLesson(WORKER, 11L))
                .thenReturn(Optional.of(record(11L, "COMPLETED", 100)));
        when(trainingCourseMapper.findPublishedById(COURSE)).thenReturn(Optional.of(course(1L)));
        when(trainingCertificationMapper.findById(1L)).thenReturn(Optional.of(activeCert()));
        when(workerCertificationMapper.findByWorkerAndCert(WORKER, 1L)).thenReturn(Optional.empty());

        LessonCompleteCmd cmd = new LessonCompleteCmd();
        cmd.setLessonId(12L);
        trainingService.markComplete(WORKER, cmd);

        verify(workerCertificationMapper).insert(any(WorkerCertification.class));
    }

    @Test
    void courseOneLessonInProgress_doesNotGrant() {
        TrainingLesson l1 = lesson(11L, "DOCUMENT", 1);
        TrainingLesson l2 = lesson(12L, "DOCUMENT", 2);
        when(trainingLessonMapper.findById(11L)).thenReturn(Optional.of(l1));
        when(workerLessonRecordMapper.findByWorkerAndLesson(WORKER, 11L)).thenReturn(Optional.empty());
        when(trainingLessonMapper.findByCourseId(COURSE)).thenReturn(List.of(l1, l2));

        LessonCompleteCmd cmd = new LessonCompleteCmd();
        cmd.setLessonId(11L);
        trainingService.markComplete(WORKER, cmd);

        verify(workerCertificationMapper, never()).insert(any());
        verify(workerCertificationMapper, never()).update(any());
    }

    @Test
    void alreadyActiveCert_notGrantedAgain() {
        TrainingLesson l1 = lesson(11L, "DOCUMENT", 1);
        TrainingLesson l2 = lesson(12L, "DOCUMENT", 2);
        when(trainingLessonMapper.findById(12L)).thenReturn(Optional.of(l2));
        when(workerLessonRecordMapper.findByWorkerAndLesson(WORKER, 12L))
                .thenReturn(Optional.of(record(12L, "IN_PROGRESS", 0)));
        when(trainingLessonMapper.findByCourseId(COURSE)).thenReturn(List.of(l1, l2));
        when(workerLessonRecordMapper.findByWorkerAndLesson(WORKER, 11L))
                .thenReturn(Optional.of(record(11L, "COMPLETED", 100)));
        when(trainingCourseMapper.findPublishedById(COURSE)).thenReturn(Optional.of(course(1L)));
        WorkerCertification existing = new WorkerCertification();
        existing.setStatus("ACTIVE");
        existing.setExpiresAt(LocalDateTime.now().plusDays(10));
        when(workerCertificationMapper.findByWorkerAndCert(WORKER, 1L)).thenReturn(Optional.of(existing));

        LessonCompleteCmd cmd = new LessonCompleteCmd();
        cmd.setLessonId(12L);
        trainingService.markComplete(WORKER, cmd);

        verify(workerCertificationMapper, never()).insert(any());
        verify(workerCertificationMapper, never()).update(any());
    }
}
