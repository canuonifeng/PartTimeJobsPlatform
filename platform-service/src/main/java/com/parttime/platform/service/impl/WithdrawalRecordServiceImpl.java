package com.parttime.platform.service.impl;

import com.parttime.platform.mapper.WithdrawalRecordMapper;
import com.parttime.platform.pojo.entity.WithdrawalRecord;
import com.parttime.platform.pojo.vo.PageVO;
import com.parttime.platform.pojo.vo.WithdrawalRecordVO;
import com.parttime.platform.service.WithdrawalRecordService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WithdrawalRecordServiceImpl implements WithdrawalRecordService {

    @Resource
    private WithdrawalRecordMapper withdrawalRecordMapper;

    @Override
    public PageVO<WithdrawalRecordVO> listRecords(Long workerId, String status, LocalDateTime startTime, LocalDateTime endTime, int page, int pageSize) {
        int offset = Math.max(page - 1, 0) * pageSize;
        List<WithdrawalRecordVO> records = withdrawalRecordMapper.findPage(workerId, status, startTime, endTime, offset, pageSize)
                .stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        long total = withdrawalRecordMapper.countPage(workerId, status, startTime, endTime);
        return new PageVO<>(records, total);
    }

    private WithdrawalRecordVO toVO(WithdrawalRecord record) {
        WithdrawalRecordVO vo = new WithdrawalRecordVO();
        BeanUtils.copyProperties(record, vo);
        return vo;
    }
}
