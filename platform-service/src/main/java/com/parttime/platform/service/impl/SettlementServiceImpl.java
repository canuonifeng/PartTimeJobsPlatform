package com.parttime.platform.service.impl;

import com.parttime.platform.exception.BusinessException;
import com.parttime.platform.mapper.EnterpriseBalanceTransactionMapper;
import com.parttime.platform.pojo.cmd.SettlementQueryCmd;
import com.parttime.platform.pojo.entity.EnterpriseBalanceTransaction;
import com.parttime.platform.pojo.vo.SettlementVO;
import com.parttime.platform.pojo.vo.TransactionVO;
import com.parttime.platform.service.SettlementService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SettlementServiceImpl implements SettlementService {

    @Resource
    private EnterpriseBalanceTransactionMapper enterpriseBalanceTransactionMapper;

    @Override
    public List<SettlementVO> list(SettlementQueryCmd cmd) {
        List<TransactionVO> rows = enterpriseBalanceTransactionMapper.findByFilters(
                "SETTLEMENT", null, null, cmd.getCompanyId(), cmd.getKeyword());
        return rows.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public SettlementVO detail(Long id) {
        TransactionVO row = enterpriseBalanceTransactionMapper.findVoById(id)
                .orElseThrow(() -> new BusinessException("结算记录不存在: " + id));
        return toVO(row);
    }

    @Override
    public void confirm(Long id) {
        enterpriseBalanceTransactionMapper.findVoById(id)
                .orElseThrow(() -> new BusinessException("结算记录不存在: " + id));
    }

    @Override
    @Transactional
    public void cancel(Long id, String reason) {
        EnterpriseBalanceTransaction original = enterpriseBalanceTransactionMapper.findEntityById(id)
                .orElseThrow(() -> new BusinessException("结算记录不存在: " + id));
        if (!"SETTLEMENT".equals(original.getType())) {
            throw new BusinessException("仅结算流水可撤销: " + id);
        }
        BigDecimal refund = original.getAmount().abs();
        EnterpriseBalanceTransaction reversal = new EnterpriseBalanceTransaction();
        reversal.setCompanyId(original.getCompanyId());
        reversal.setAmount(refund);
        reversal.setType("SETTLEMENT_REVERSE");
        reversal.setDescription("撤销结算: 原单#" + id + (reason != null && !reason.isEmpty() ? "，原因: " + reason : ""));
        reversal.setCreatedAt(LocalDateTime.now());
        enterpriseBalanceTransactionMapper.insert(reversal);
    }

    private SettlementVO toVO(TransactionVO t) {
        SettlementVO vo = new SettlementVO();
        vo.setId(t.getId());
        vo.setSettlementNo("STL" + t.getId());
        vo.setCompanyName(t.getRelatedName());
        vo.setAmount(t.getAmount() != null ? t.getAmount().abs() : null);
        vo.setType(t.getType());
        vo.setDescription(t.getRemark());
        vo.setStatus("PAID");
        vo.setCreatedAt(t.getCreatedAt());
        return vo;
    }
}
