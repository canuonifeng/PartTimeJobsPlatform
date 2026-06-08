package com.parttime.cservice.mapper;

import com.parttime.cservice.pojo.entity.WorkerBankCard;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface WorkerBankCardMapper {
    int insert(WorkerBankCard card);
    int update(WorkerBankCard card);
    Optional<WorkerBankCard> findByWorkerId(Long workerId);
    List<WorkerBankCard> findAllByWorkerId(Long workerId);
    int deleteByWorkerId(Long workerId);
}
