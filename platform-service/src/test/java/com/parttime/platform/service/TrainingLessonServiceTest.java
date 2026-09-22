package com.parttime.platform.service;

import com.parttime.platform.exception.BusinessException;
import com.parttime.platform.mapper.QuestionBankMapper;
import com.parttime.platform.mapper.TrainingLessonMapper;
import com.parttime.platform.pojo.cmd.TrainingLessonCreateCmd;
import com.parttime.platform.pojo.cmd.TrainingLessonQueryCmd;
import com.parttime.platform.pojo.cmd.TrainingLessonUpdateCmd;
import com.parttime.platform.pojo.entity.QuestionBank;
import com.parttime.platform.pojo.entity.TrainingLesson;
import com.parttime.platform.pojo.vo.TrainingLessonVO;
import com.parttime.platform.service.impl.TrainingLessonServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingLessonServiceTest {

    @Mock
    private TrainingLessonMapper trainingLessonMapper;

    @Mock
    private QuestionBankMapper questionBankMapper;

    @InjectMocks
    private TrainingLessonServiceImpl service;

    private QuestionBank activeBank() {
        QuestionBank bank = new QuestionBank();
        bank.setId(1L);
        bank.setName("考试题库");
        bank.setStatus("ACTIVE");
        return bank;
    }

    private TrainingLessonCreateCmd.ExamRuleDTO rule(String type, int count, int scorePer) {
        TrainingLessonCreateCmd.ExamRuleDTO r = new TrainingLessonCreateCmd.ExamRuleDTO();
        r.setQuestionType(type);
        r.setCount(count);
        r.setScorePer(scorePer);
        return r;
    }

    private TrainingLessonCreateCmd.ExamConfigDTO examConfig(Long bankId, int duration, int passScore,
                                                             List<TrainingLessonCreateCmd.ExamRuleDTO> rules) {
        TrainingLessonCreateCmd.ExamConfigDTO c = new TrainingLessonCreateCmd.ExamConfigDTO();
        c.setBankId(bankId);
        c.setDurationMinutes(duration);
        c.setPassScore(passScore);
        c.setRules(rules);
        return c;
    }

    private TrainingLesson lesson(Long id, String status) {
        TrainingLesson l = new TrainingLesson();
        l.setId(id);
        l.setCourseId(1L);
        l.setLessonType("VIDEO");
        l.setTitle("视频课");
        l.setStatus(status);
        l.setSortOrder(0);
        return l;
    }

    @Test
    void list_shouldReturnLessonsByCourseId() {
        when(trainingLessonMapper.findByCourseId(1L)).thenReturn(List.of(lesson(1L, "DRAFT")));
        TrainingLessonQueryCmd cmd = new TrainingLessonQueryCmd();
        cmd.setCourseId(1L);

        List<TrainingLessonVO> result = service.list(cmd);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    void list_nullCourseId_shouldThrow() {
        TrainingLessonQueryCmd cmd = new TrainingLessonQueryCmd();
        assertThatThrownBy(() -> service.list(cmd))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("课程ID不能为空");
    }

    @Test
    void create_video_shouldSucceed() {
        TrainingLessonCreateCmd cmd = new TrainingLessonCreateCmd();
        cmd.setCourseId(1L);
        cmd.setLessonType("VIDEO");
        cmd.setTitle("视频课");
        cmd.setMediaUrl("https://example.com/video.mp4");
        cmd.setDurationMinutes(30);

        TrainingLessonVO result = service.create(cmd);

        assertThat(result.getStatus()).isEqualTo("DRAFT");
        assertThat(result.getLessonType()).isEqualTo("VIDEO");
        assertThat(result.getTotalScore()).isEqualTo(0);
        verify(trainingLessonMapper).insert(any(TrainingLesson.class));
    }

    @Test
    void create_document_shouldSucceed() {
        TrainingLessonCreateCmd cmd = new TrainingLessonCreateCmd();
        cmd.setCourseId(1L);
        cmd.setLessonType("DOCUMENT");
        cmd.setTitle("文档课");
        cmd.setContent("课程正文内容");

        TrainingLessonVO result = service.create(cmd);

        assertThat(result.getStatus()).isEqualTo("DRAFT");
        verify(trainingLessonMapper).insert(any(TrainingLesson.class));
    }

    @Test
    void create_exam_shouldSucceed() {
        when(questionBankMapper.findById(1L)).thenReturn(Optional.of(activeBank()));
        TrainingLessonCreateCmd cmd = new TrainingLessonCreateCmd();
        cmd.setCourseId(1L);
        cmd.setLessonType("EXAM");
        cmd.setTitle("期末考试");
        cmd.setExamConfig(examConfig(1L, 60, 60,
                List.of(rule("SINGLE_CHOICE", 10, 2), rule("JUDGE", 5, 1))));

        TrainingLessonVO result = service.create(cmd);

        assertThat(result.getStatus()).isEqualTo("DRAFT");
        assertThat(result.getTotalScore()).isEqualTo(25);
        assertThat(result.getExamConfig().getBankId()).isEqualTo(1L);
        verify(trainingLessonMapper).insert(any(TrainingLesson.class));
    }

    @Test
    void create_examMissingConfig_shouldThrow() {
        TrainingLessonCreateCmd cmd = new TrainingLessonCreateCmd();
        cmd.setCourseId(1L);
        cmd.setLessonType("EXAM");
        cmd.setTitle("期末考试");

        assertThatThrownBy(() -> service.create(cmd))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("考试课时必须配置考试规则");
        verify(trainingLessonMapper, never()).insert(any());
    }

    @Test
    void create_examBankNotFound_shouldThrow() {
        when(questionBankMapper.findById(99L)).thenReturn(Optional.empty());
        TrainingLessonCreateCmd cmd = new TrainingLessonCreateCmd();
        cmd.setCourseId(1L);
        cmd.setLessonType("EXAM");
        cmd.setTitle("期末考试");
        cmd.setExamConfig(examConfig(99L, 60, 60, List.of(rule("SINGLE_CHOICE", 10, 2))));

        assertThatThrownBy(() -> service.create(cmd))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("题库不存在或已停用");
        verify(trainingLessonMapper, never()).insert(any());
    }

    @Test
    void create_examBankDisabled_shouldThrow() {
        QuestionBank disabled = activeBank();
        disabled.setStatus("DISABLED");
        when(questionBankMapper.findById(1L)).thenReturn(Optional.of(disabled));
        TrainingLessonCreateCmd cmd = new TrainingLessonCreateCmd();
        cmd.setCourseId(1L);
        cmd.setLessonType("EXAM");
        cmd.setTitle("期末考试");
        cmd.setExamConfig(examConfig(1L, 60, 60, List.of(rule("SINGLE_CHOICE", 10, 2))));

        assertThatThrownBy(() -> service.create(cmd))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("题库不存在或已停用");
        verify(trainingLessonMapper, never()).insert(any());
    }

    @Test
    void create_examDurationZero_shouldThrow() {
        when(questionBankMapper.findById(1L)).thenReturn(Optional.of(activeBank()));
        TrainingLessonCreateCmd cmd = new TrainingLessonCreateCmd();
        cmd.setCourseId(1L);
        cmd.setLessonType("EXAM");
        cmd.setTitle("期末考试");
        cmd.setExamConfig(examConfig(1L, 0, 60, List.of(rule("SINGLE_CHOICE", 10, 2))));

        assertThatThrownBy(() -> service.create(cmd))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("考试时长必须大于0");
        verify(trainingLessonMapper, never()).insert(any());
    }

    @Test
    void create_examNegativePassScore_shouldThrow() {
        when(questionBankMapper.findById(1L)).thenReturn(Optional.of(activeBank()));
        TrainingLessonCreateCmd cmd = new TrainingLessonCreateCmd();
        cmd.setCourseId(1L);
        cmd.setLessonType("EXAM");
        cmd.setTitle("期末考试");
        cmd.setExamConfig(examConfig(1L, 60, -5, List.of(rule("SINGLE_CHOICE", 10, 2))));

        assertThatThrownBy(() -> service.create(cmd))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("及格分不能为负");
        verify(trainingLessonMapper, never()).insert(any());
    }

    @Test
    void create_examEmptyRules_shouldThrow() {
        when(questionBankMapper.findById(1L)).thenReturn(Optional.of(activeBank()));
        TrainingLessonCreateCmd cmd = new TrainingLessonCreateCmd();
        cmd.setCourseId(1L);
        cmd.setLessonType("EXAM");
        cmd.setTitle("期末考试");
        cmd.setExamConfig(examConfig(1L, 60, 60, List.of()));

        assertThatThrownBy(() -> service.create(cmd))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("考试规则不能为空");
        verify(trainingLessonMapper, never()).insert(any());
    }

    @Test
    void create_examInvalidRuleType_shouldThrow() {
        when(questionBankMapper.findById(1L)).thenReturn(Optional.of(activeBank()));
        TrainingLessonCreateCmd cmd = new TrainingLessonCreateCmd();
        cmd.setCourseId(1L);
        cmd.setLessonType("EXAM");
        cmd.setTitle("期末考试");
        cmd.setExamConfig(examConfig(1L, 60, 60, List.of(rule("INVALID_TYPE", 10, 2))));

        assertThatThrownBy(() -> service.create(cmd))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("题型规则不合法");
        verify(trainingLessonMapper, never()).insert(any());
    }

    @Test
    void create_examRuleCountZero_shouldThrow() {
        when(questionBankMapper.findById(1L)).thenReturn(Optional.of(activeBank()));
        TrainingLessonCreateCmd cmd = new TrainingLessonCreateCmd();
        cmd.setCourseId(1L);
        cmd.setLessonType("EXAM");
        cmd.setTitle("期末考试");
        cmd.setExamConfig(examConfig(1L, 60, 60, List.of(rule("SINGLE_CHOICE", 0, 2))));

        assertThatThrownBy(() -> service.create(cmd))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("题型规则不合法");
        verify(trainingLessonMapper, never()).insert(any());
    }

    @Test
    void create_examRuleScorePerZero_shouldThrow() {
        when(questionBankMapper.findById(1L)).thenReturn(Optional.of(activeBank()));
        TrainingLessonCreateCmd cmd = new TrainingLessonCreateCmd();
        cmd.setCourseId(1L);
        cmd.setLessonType("EXAM");
        cmd.setTitle("期末考试");
        cmd.setExamConfig(examConfig(1L, 60, 60, List.of(rule("SINGLE_CHOICE", 10, 0))));

        assertThatThrownBy(() -> service.create(cmd))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("题型规则不合法");
        verify(trainingLessonMapper, never()).insert(any());
    }

    @Test
    void create_videoMissingMediaUrl_shouldThrow() {
        TrainingLessonCreateCmd cmd = new TrainingLessonCreateCmd();
        cmd.setCourseId(1L);
        cmd.setLessonType("VIDEO");
        cmd.setTitle("视频课");
        cmd.setDurationMinutes(30);

        assertThatThrownBy(() -> service.create(cmd))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("媒体地址");
        verify(trainingLessonMapper, never()).insert(any());
    }

    @Test
    void create_videoDurationZero_shouldThrow() {
        TrainingLessonCreateCmd cmd = new TrainingLessonCreateCmd();
        cmd.setCourseId(1L);
        cmd.setLessonType("VIDEO");
        cmd.setTitle("视频课");
        cmd.setMediaUrl("https://example.com/video.mp4");
        cmd.setDurationMinutes(0);

        assertThatThrownBy(() -> service.create(cmd))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("课时时长必须大于0");
        verify(trainingLessonMapper, never()).insert(any());
    }

    @Test
    void create_documentMissingContent_shouldThrow() {
        TrainingLessonCreateCmd cmd = new TrainingLessonCreateCmd();
        cmd.setCourseId(1L);
        cmd.setLessonType("DOCUMENT");
        cmd.setTitle("文档课");

        assertThatThrownBy(() -> service.create(cmd))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("正文内容");
        verify(trainingLessonMapper, never()).insert(any());
    }

    @Test
    void update_shouldUpdateSuccessfully() {
        when(trainingLessonMapper.findById(1L)).thenReturn(Optional.of(lesson(1L, "DRAFT")));
        TrainingLessonUpdateCmd cmd = new TrainingLessonUpdateCmd();
        cmd.setId(1L);
        cmd.setCourseId(1L);
        cmd.setLessonType("VIDEO");
        cmd.setTitle("新标题");
        cmd.setMediaUrl("https://example.com/new.mp4");
        cmd.setDurationMinutes(45);

        TrainingLessonVO result = service.update(cmd);

        assertThat(result.getTitle()).isEqualTo("新标题");
        verify(trainingLessonMapper).update(any(TrainingLesson.class));
    }

    @Test
    void update_notFound_shouldThrow() {
        TrainingLessonUpdateCmd cmd = new TrainingLessonUpdateCmd();
        cmd.setId(999L);
        when(trainingLessonMapper.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(cmd))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("课时不存在");
        verify(trainingLessonMapper, never()).update(any());
    }

    @Test
    void publish_draftToPublished_shouldUpdateStatus() {
        when(trainingLessonMapper.findById(1L)).thenReturn(Optional.of(lesson(1L, "DRAFT")));

        service.publish(1L);

        verify(trainingLessonMapper).updateStatus(1L, "PUBLISHED");
    }

    @Test
    void publish_alreadyPublished_shouldThrow() {
        when(trainingLessonMapper.findById(1L)).thenReturn(Optional.of(lesson(1L, "PUBLISHED")));

        assertThatThrownBy(() -> service.publish(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("仅草稿状态可发布");
        verify(trainingLessonMapper, never()).updateStatus(anyLong(), any());
    }

    @Test
    void offline_publishedToOffline_shouldUpdateStatus() {
        when(trainingLessonMapper.findById(1L)).thenReturn(Optional.of(lesson(1L, "PUBLISHED")));

        service.offline(1L);

        verify(trainingLessonMapper).updateStatus(1L, "OFFLINE");
    }

    @Test
    void offline_draft_shouldThrow() {
        when(trainingLessonMapper.findById(1L)).thenReturn(Optional.of(lesson(1L, "DRAFT")));

        assertThatThrownBy(() -> service.offline(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("仅已发布状态可下线");
        verify(trainingLessonMapper, never()).updateStatus(anyLong(), any());
    }

    @Test
    void delete_draft_shouldDelete() {
        when(trainingLessonMapper.findById(1L)).thenReturn(Optional.of(lesson(1L, "DRAFT")));

        service.delete(1L);

        verify(trainingLessonMapper).deleteById(1L);
    }

    @Test
    void delete_offline_shouldDelete() {
        when(trainingLessonMapper.findById(1L)).thenReturn(Optional.of(lesson(1L, "OFFLINE")));

        service.delete(1L);

        verify(trainingLessonMapper).deleteById(1L);
    }

    @Test
    void delete_published_shouldThrow() {
        when(trainingLessonMapper.findById(1L)).thenReturn(Optional.of(lesson(1L, "PUBLISHED")));

        assertThatThrownBy(() -> service.delete(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("已发布课时请先下线");
        verify(trainingLessonMapper, never()).deleteById(anyLong());
    }
}
