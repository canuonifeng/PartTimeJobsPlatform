package com.parttime.platform.service.impl;

import com.parttime.platform.pojo.vo.WithdrawalRecordVO;
import com.parttime.platform.service.WithdrawalRecordService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class WithdrawalRecordServiceImpl implements WithdrawalRecordService {

    @Override
    public List<WithdrawalRecordVO> list(String status, String keyword) {
        List<WithdrawalRecordVO> list = new ArrayList<>();
        String[] statuses = {"PENDING", "APPROVED", "REJECTED", "COMPLETED"};
        String[] platforms = {"ALIPAY", "WECHAT", "BANK"};
        for (int i = 1; i <= 20; i++) {
            WithdrawalRecordVO vo = new WithdrawalRecordVO();
            vo.setId((long) i);
            vo.setWorkerId((long) (i % 100 + 1));
            vo.setWorkerName("工人" + (i % 100 + 1));
            vo.setWorkerPhone("138" + String.format("%08d", i));
            vo.setAmount(new BigDecimal((i % 10 + 1) * 100));
            vo.setStatus(statuses[i % 4]);
            vo.setBankInfo(platforms[i % 3] + " ****" + (1000 + i));
            vo.setThirdPartyPlatform(platforms[i % 3]);
            vo.setThirdPartySerialNo("TPSN" + System.currentTimeMillis() + i);
            vo.setRequestedAt(LocalDateTime.now().minusHours(i));
            vo.setCreatedAt(LocalDateTime.now().minusHours(i));
            list.add(vo);
        }
        return list;
    }

    @Override
    public WithdrawalRecordVO detail(Long id) {
        WithdrawalRecordVO vo = new WithdrawalRecordVO();
        vo.setId(id);
        vo.setWorkerId(1L);
        vo.setWorkerName("测试工人");
        vo.setWorkerPhone("13800138000");
        vo.setAmount(new BigDecimal("500.00"));
        vo.setStatus("PENDING");
        vo.setBankInfo("支付宝 ****1234");
        vo.setThirdPartyPlatform("ALIPAY");
        vo.setThirdPartySerialNo("TPSN" + System.currentTimeMillis());
        vo.setRemark("测试提现申请");
        vo.setRequestedAt(LocalDateTime.now().minusHours(2));
        vo.setCreatedAt(LocalDateTime.now().minusHours(2));
        return vo;
    }

    @Override
    public void approve(Long id) {
    }

    @Override
    public void reject(Long id, String reason) {
    }
}
