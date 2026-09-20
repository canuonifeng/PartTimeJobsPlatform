package com.parttime.platform.service.impl;

import com.parttime.platform.exception.BusinessException;
import com.parttime.platform.mapper.FaqMapper;
import com.parttime.platform.pojo.cmd.FaqQueryCmd;
import com.parttime.platform.pojo.cmd.FaqSaveCmd;
import com.parttime.platform.pojo.cmd.FaqSortCmd;
import com.parttime.platform.pojo.entity.Faq;
import com.parttime.platform.pojo.vo.FaqVO;
import com.parttime.platform.service.FaqService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FaqServiceImpl implements FaqService {

    @Resource
    private FaqMapper faqMapper;

    @Override
    public List<FaqVO> list(FaqQueryCmd cmd) {
        String status = cmd != null && (cmd.getStatus() == null || cmd.getStatus().isBlank()) ? null : (cmd == null ? null : cmd.getStatus());
        String category = cmd != null && (cmd.getCategory() == null || cmd.getCategory().isBlank()) ? null : (cmd == null ? null : cmd.getCategory());
        String keyword = cmd != null && (cmd.getKeyword() == null || cmd.getKeyword().isBlank()) ? null : (cmd == null ? null : cmd.getKeyword());
        return faqMapper.findByFilters(keyword, category, status).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public FaqVO detail(Long id) {
        return toVO(require(id));
    }

    @Override
    public FaqVO create(FaqSaveCmd cmd) {
        Faq faq = new Faq();
        fill(faq, cmd);
        faq.setStatus(normalizeStatus(cmd.getStatus()));
        faqMapper.insert(faq);
        return toVO(faq);
    }

    @Override
    public FaqVO update(FaqSaveCmd cmd) {
        Faq faq = require(cmd.getId());
        fill(faq, cmd);
        faq.setStatus(normalizeStatus(cmd.getStatus()));
        faqMapper.update(faq);
        return toVO(faq);
    }

    @Override
    public void sort(FaqSortCmd cmd) {
        require(cmd.getId());
        faqMapper.updateSortOrder(cmd.getId(), cmd.getSortOrder() == null ? 0 : cmd.getSortOrder());
    }

    @Override
    public void delete(Long id) {
        require(id);
        faqMapper.deleteById(id);
    }

    private Faq require(Long id) {
        return faqMapper.findById(id)
                .orElseThrow(() -> new BusinessException("FAQ不存在: " + id));
    }

    private void fill(Faq faq, FaqSaveCmd cmd) {
        faq.setQuestion(cmd.getQuestion());
        faq.setAnswer(cmd.getAnswer());
        faq.setCategory(cmd.getCategory() == null || cmd.getCategory().isBlank() ? "GENERAL" : cmd.getCategory());
        faq.setSortOrder(cmd.getSortOrder() == null ? 0 : cmd.getSortOrder());
    }

    private String normalizeStatus(String status) {
        return status == null || status.isBlank() ? "ACTIVE" : status;
    }

    private FaqVO toVO(Faq f) {
        FaqVO vo = new FaqVO();
        vo.setId(f.getId());
        vo.setQuestion(f.getQuestion());
        vo.setAnswer(f.getAnswer());
        vo.setCategory(f.getCategory());
        vo.setSortOrder(f.getSortOrder());
        vo.setViewCount(f.getViewCount());
        vo.setHelpfulCount(f.getHelpfulCount());
        vo.setNotHelpfulCount(f.getNotHelpfulCount());
        vo.setStatus(f.getStatus());
        vo.setOperatorName(f.getOperatorName());
        vo.setCreatedAt(f.getCreatedAt());
        vo.setUpdatedAt(f.getUpdatedAt());
        return vo;
    }
}
