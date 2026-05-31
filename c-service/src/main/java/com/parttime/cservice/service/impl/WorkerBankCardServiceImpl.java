package com.parttime.cservice.service.impl;

import com.parttime.cservice.mapper.WorkerBankCardMapper;
import com.parttime.cservice.pojo.cmd.WorkerBankCardCmd;
import com.parttime.cservice.pojo.entity.WorkerBankCard;
import com.parttime.cservice.service.WorkerBankCardService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class WorkerBankCardServiceImpl implements WorkerBankCardService {

    @Resource
    private WorkerBankCardMapper workerBankCardMapper;

    @Override
    public WorkerBankCard get(Long workerId) {
        return workerBankCardMapper.findByWorkerId(workerId).orElse(null);
    }

    @Override
    public WorkerBankCard upsert(Long workerId, WorkerBankCardCmd cmd) {
        if (cmd == null
                || isBlank(cmd.getCardHolder())
                || isBlank(cmd.getCardNumber())
                || isBlank(cmd.getBankName())) {
            throw new RuntimeException("持卡人、卡号、银行名称必填");
        }
        Optional<WorkerBankCard> existing = workerBankCardMapper.findByWorkerId(workerId);
        WorkerBankCard card = existing.orElseGet(WorkerBankCard::new);
        card.setWorkerId(workerId);
        card.setCardHolder(cmd.getCardHolder());
        card.setCardNumber(cmd.getCardNumber());
        card.setBankName(cmd.getBankName());
        card.setBankBranch(cmd.getBankBranch());
        if (existing.isPresent()) {
            workerBankCardMapper.update(card);
        } else {
            workerBankCardMapper.insert(card);
        }
        return card;
    }

    @Override
    public void delete(Long workerId) {
        workerBankCardMapper.deleteByWorkerId(workerId);
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
