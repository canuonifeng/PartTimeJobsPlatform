package com.parttime.platform.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.parttime.platform.exception.BusinessException;
import com.parttime.platform.mapper.QuestionBankQuestionMapper;
import com.parttime.platform.pojo.cmd.QuestionCreateCmd;
import com.parttime.platform.pojo.cmd.QuestionListCmd;
import com.parttime.platform.pojo.cmd.QuestionUpdateCmd;
import com.parttime.platform.pojo.entity.QuestionBankQuestion;
import com.parttime.platform.pojo.vo.QuestionVO;
import com.parttime.platform.service.QuestionBankQuestionService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestionBankQuestionServiceImpl implements QuestionBankQuestionService {

    private static final Logger log = LoggerFactory.getLogger(QuestionBankQuestionServiceImpl.class);

    private static final String DRAFT = "DRAFT";
    private static final String PUBLISHED = "PUBLISHED";
    private static final String OFFLINE = "OFFLINE";
    private static final String MULTIPLE_CHOICE = "MULTIPLE_CHOICE";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    private QuestionBankQuestionMapper questionBankQuestionMapper;

    @Override
    public List<QuestionVO> list(QuestionListCmd cmd) {
        if (cmd.getBankId() == null) {
            throw new BusinessException("题库ID不能为空");
        }
        return questionBankQuestionMapper.findByBankId(cmd.getBankId()).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public QuestionVO create(QuestionCreateCmd cmd) {
        requireBankId(cmd.getBankId());
        requireStem(cmd.getStem());
        QuestionBankQuestion question = new QuestionBankQuestion();
        question.setBankId(cmd.getBankId());
        question.setQuestionType(cmd.getQuestionType());
        question.setStem(cmd.getStem().trim());
        question.setOptionsJson(toOptionsJson(cmd.getOptions()));
        question.setAnswer(normalizeAnswer(cmd.getQuestionType(), cmd.getAnswer()));
        question.setAnalysis(cmd.getAnalysis());
        question.setStatus(DRAFT);
        question.setSortOrder(cmd.getSortOrder() == null ? 0 : cmd.getSortOrder());
        questionBankQuestionMapper.insert(question);
        return toVO(question);
    }

    @Override
    public QuestionVO update(QuestionUpdateCmd cmd) {
        QuestionBankQuestion question = questionBankQuestionMapper.findById(cmd.getId())
                .orElseThrow(() -> new BusinessException("题目不存在: " + cmd.getId()));
        requireStem(cmd.getStem());
        if (cmd.getBankId() != null) {
            question.setBankId(cmd.getBankId());
        }
        question.setQuestionType(cmd.getQuestionType());
        question.setStem(cmd.getStem().trim());
        question.setOptionsJson(toOptionsJson(cmd.getOptions()));
        question.setAnswer(normalizeAnswer(cmd.getQuestionType(), cmd.getAnswer()));
        question.setAnalysis(cmd.getAnalysis());
        question.setSortOrder(cmd.getSortOrder() == null ? question.getSortOrder() : cmd.getSortOrder());
        questionBankQuestionMapper.update(question);
        return toVO(question);
    }

    @Override
    public void delete(Long id) {
        QuestionBankQuestion question = questionBankQuestionMapper.findById(id)
                .orElseThrow(() -> new BusinessException("题目不存在: " + id));
        if (PUBLISHED.equals(question.getStatus())) {
            throw new BusinessException("仅草稿或已下线题目可删除，已发布题目请先下线");
        }
        questionBankQuestionMapper.deleteById(id);
    }

    @Override
    public void publish(Long id) {
        QuestionBankQuestion question = questionBankQuestionMapper.findById(id)
                .orElseThrow(() -> new BusinessException("题目不存在: " + id));
        if (!DRAFT.equals(question.getStatus())) {
            throw new BusinessException("仅草稿状态可发布");
        }
        questionBankQuestionMapper.updateStatus(id, PUBLISHED);
    }

    @Override
    public void offline(Long id) {
        QuestionBankQuestion question = questionBankQuestionMapper.findById(id)
                .orElseThrow(() -> new BusinessException("题目不存在: " + id));
        if (!PUBLISHED.equals(question.getStatus())) {
            throw new BusinessException("仅已发布状态可下线");
        }
        questionBankQuestionMapper.updateStatus(id, OFFLINE);
    }

    private void requireBankId(Long bankId) {
        if (bankId == null) {
            throw new BusinessException("题库ID不能为空");
        }
    }

    private void requireStem(String stem) {
        if (stem == null || stem.isBlank()) {
            throw new BusinessException("题干不能为空");
        }
    }

    private String toOptionsJson(List<?> options) {
        if (options == null || options.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(options);
        } catch (Exception e) {
            log.warn("options serialize failed", e);
            return "[]";
        }
    }

    private String normalizeAnswer(String questionType, String answer) {
        if (answer == null || answer.isBlank()) {
            throw new BusinessException("答案不能为空");
        }
        String trimmed = answer.trim();
        if (MULTIPLE_CHOICE.equals(questionType)) {
            if (trimmed.startsWith("[")) {
                try {
                    List<?> parsed = objectMapper.readValue(trimmed, List.class);
                    return objectMapper.writeValueAsString(parsed);
                } catch (Exception e) {
                    throw new BusinessException("答案格式非法");
                }
            }
            List<String> keys = Arrays.stream(trimmed.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            try {
                return objectMapper.writeValueAsString(keys);
            } catch (Exception e) {
                throw new BusinessException("答案格式非法");
            }
        }
        return trimmed;
    }

    private QuestionVO toVO(QuestionBankQuestion question) {
        QuestionVO vo = new QuestionVO();
        vo.setId(question.getId());
        vo.setBankId(question.getBankId());
        vo.setQuestionType(question.getQuestionType());
        vo.setStem(question.getStem());
        vo.setOptions(parseOptions(question.getOptionsJson()));
        vo.setAnswer(question.getAnswer());
        vo.setAnalysis(question.getAnalysis());
        vo.setStatus(question.getStatus());
        vo.setSortOrder(question.getSortOrder());
        vo.setCreatedAt(question.getCreatedAt());
        vo.setUpdatedAt(question.getUpdatedAt());
        return vo;
    }

    private List<QuestionVO.OptionDTO> parseOptions(String optionsJson) {
        if (optionsJson == null || optionsJson.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(optionsJson, new TypeReference<List<QuestionVO.OptionDTO>>() {});
        } catch (Exception e) {
            log.warn("options parse failed: {}", optionsJson, e);
            return new ArrayList<>();
        }
    }
}
