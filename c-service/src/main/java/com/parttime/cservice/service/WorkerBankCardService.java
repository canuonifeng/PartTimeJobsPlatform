package com.parttime.cservice.service;

import com.parttime.cservice.pojo.cmd.WorkerBankCardCmd;
import com.parttime.cservice.pojo.entity.WorkerBankCard;

public interface WorkerBankCardService {
    WorkerBankCard get(Long workerId);
    WorkerBankCard upsert(Long workerId, WorkerBankCardCmd cmd);
    void delete(Long workerId);
}
