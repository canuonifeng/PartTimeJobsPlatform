package com.parttime.platform.service.impl;

import com.parttime.platform.exception.BusinessException;
import com.parttime.platform.mapper.QuestionBankMapper;
import com.parttime.platform.pojo.cmd.QuestionBankCreateCmd;
import com.parttime.platform.pojo.cmd.QuestionBankUpdateCmd;
import com.parttime.platform.pojo.entity.QuestionBank;
import com.parttime.platform.pojo.vo.QuestionBankVO;
import com.parttime.platform.service.QuestionBankService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

@Service
public class QuestionBankServiceImpl implements QuestionBankService {

    private static final String ACTIVE = "ACTIVE";
    private static final String DISABLED = "DISABLED";

    @Resource
    private QuestionBankMapper questionBankMapper;

    @Override
    public List<QuestionBankVO> list() {
        return questionBankMapper.findAll().stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    public QuestionBankVO create(QuestionBankCreateCmd cmd) {
        String name = requireName(cmd.getName());
        questionBankMapper.findByName(name)
                .ifPresent(existing -> {
                    throw new BusinessException("题库名称已存在: " + name);
                });
        QuestionBank bank = new QuestionBank();
        bank.setName(name);
        bank.setDescription(cmd.getDescription());
        bank.setStatus(ACTIVE);
        questionBankMapper.insert(bank);
        return toVO(bank);
    }

    @Override
    public QuestionBankVO update(QuestionBankUpdateCmd cmd) {
        QuestionBank bank = questionBankMapper.findById(cmd.getId())
                .orElseThrow(() -> new BusinessException("题库不存在: " + cmd.getId()));
        String name = requireName(cmd.getName());
        questionBankMapper.findByName(name)
                .filter(existing -> !existing.getId().equals(cmd.getId()))
                .ifPresent(existing -> {
                    throw new BusinessException("题库名称已存在: " + name);
                });
        bank.setName(name);
        bank.setDescription(cmd.getDescription());
        questionBankMapper.update(bank);
        return toVO(bank);
    }

    @Override
    public void toggle(Long id) {
        QuestionBank bank = questionBankMapper.findById(id)
                .orElseThrow(() -> new BusinessException("题库不存在: " + id));
        String target = ACTIVE.equals(bank.getStatus()) ? DISABLED : ACTIVE;
        questionBankMapper.updateStatus(id, target);
    }

    private String requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessException("题库名称不能为空");
        }
        return name.trim();
    }

    private QuestionBankVO toVO(QuestionBank bank) {
        QuestionBankVO vo = new QuestionBankVO();
        vo.setId(bank.getId());
        vo.setName(bank.getName());
        vo.setDescription(bank.getDescription());
        vo.setStatus(bank.getStatus());
        vo.setCount(questionBankMapper.countByBankId(bank.getId()));
        vo.setCreatedAt(bank.getCreatedAt());
        vo.setUpdatedAt(bank.getUpdatedAt());
        return vo;
    }
}
