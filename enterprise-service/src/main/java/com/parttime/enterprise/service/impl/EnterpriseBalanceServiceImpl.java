package com.parttime.enterprise.service.impl;

import com.parttime.enterprise.mapper.EnterpriseBalanceMapper;
import com.parttime.enterprise.mapper.EnterpriseBalanceTransactionMapper;
import com.parttime.enterprise.mapper.EnterpriseTopUpRecordMapper;
import com.parttime.enterprise.pojo.entity.EnterpriseBalance;
import com.parttime.enterprise.pojo.entity.EnterpriseBalanceTransaction;
import com.parttime.enterprise.pojo.entity.EnterpriseTopUpRecord;
import com.parttime.enterprise.pojo.vo.EnterpriseBalanceVO;
import com.parttime.enterprise.pojo.vo.EnterpriseTransactionVO;
import com.parttime.enterprise.pojo.vo.PageVO;
import com.parttime.enterprise.service.EnterpriseBalanceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class EnterpriseBalanceServiceImpl implements EnterpriseBalanceService {

    @Resource
    private EnterpriseBalanceMapper enterpriseBalanceMapper;

    @Resource
    private EnterpriseBalanceTransactionMapper transactionMapper;

    @Resource
    private EnterpriseTopUpRecordMapper topUpRecordMapper;

    private static final DateTimeFormatter SERIAL_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final Random RANDOM = new Random();
    private static final DateTimeFormatter BEIJING_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ZoneId BEIJING = ZoneId.of("Asia/Shanghai");

    @Override
    public EnterpriseBalanceVO getBalance(Long companyId) {
        EnterpriseBalance eb = enterpriseBalanceMapper.findByCompanyId(companyId);
        if (eb == null) {
            EnterpriseBalanceVO vo = new EnterpriseBalanceVO();
            vo.setBalance(BigDecimal.ZERO);
            vo.setCreditLimit(BigDecimal.ZERO);
            vo.setTotalTopUp(BigDecimal.ZERO);
            vo.setTotalSpent(BigDecimal.ZERO);
            vo.setUsableBalance(BigDecimal.ZERO);
            return vo;
        }
        EnterpriseBalanceVO vo = new EnterpriseBalanceVO();
        vo.setBalance(eb.getBalance());
        vo.setCreditLimit(eb.getCreditLimit());
        vo.setTotalTopUp(eb.getTotalTopUp());
        vo.setTotalSpent(eb.getTotalSpent());
        vo.setUsableBalance(eb.getBalance().add(eb.getCreditLimit()));
        return vo;
    }

    @Override
    @Transactional
    public void topUp(Long companyId, BigDecimal amount) {
        EnterpriseTopUpRecord record = new EnterpriseTopUpRecord();
        record.setCompanyId(companyId);
        record.setAmount(amount);
        record.setStatus("PROCESSING");
        record.setSerialNumber(generateTopUpSerial());
        topUpRecordMapper.insert(record);

        String serialNo = "SIM_" + System.currentTimeMillis();
        topUpRecordMapper.updateStatus(record.getId(), "COMPLETED", serialNo, "SIMULATED_PAY", LocalDateTime.now());

        EnterpriseBalance eb = enterpriseBalanceMapper.findByCompanyId(companyId);
        BigDecimal newBalance = (eb == null ? BigDecimal.ZERO : eb.getBalance()).add(amount);
        BigDecimal newTotalTopUp = (eb == null ? BigDecimal.ZERO : eb.getTotalTopUp()).add(amount);
        BigDecimal totalSpent = (eb == null ? BigDecimal.ZERO : eb.getTotalSpent());
        enterpriseBalanceMapper.upsert(companyId, newBalance, newTotalTopUp, totalSpent);

        EnterpriseBalanceTransaction txn = new EnterpriseBalanceTransaction();
        txn.setCompanyId(companyId);
        txn.setAmount(amount);
        txn.setType("TOP_UP");
        txn.setRelatedTopUpId(record.getId());
        txn.setDescription("企业充值: +" + amount + "元");
        transactionMapper.insert(txn);
    }

    @Override
    public PageVO<EnterpriseTransactionVO> getTransactions(Long companyId, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<EnterpriseBalanceTransaction> list = transactionMapper.findByCompanyIdPage(companyId, offset, pageSize);
        long total = transactionMapper.countByCompanyId(companyId);
        List<EnterpriseTransactionVO> voList = list.stream().map(txn -> {
            EnterpriseTransactionVO vo = new EnterpriseTransactionVO();
            vo.setId(txn.getId());
            vo.setAmount(txn.getAmount());
            vo.setType(txn.getType());
            vo.setRelatedBillId(txn.getRelatedBillId());
            vo.setDescription(txn.getDescription());
            if (txn.getCreatedAt() != null) {
                ZonedDateTime beijing = txn.getCreatedAt().atZone(ZoneId.systemDefault()).withZoneSameInstant(BEIJING);
                vo.setCreatedAt(beijing.format(BEIJING_FMT));
            }
            return vo;
        }).collect(Collectors.toList());
        return new PageVO<>(voList, total);
    }

    @Override
    @Transactional
    public void deduct(Long companyId, BigDecimal amount, Long relatedBillId, String description) {
        EnterpriseBalance eb = enterpriseBalanceMapper.findByCompanyId(companyId);
        BigDecimal balance = (eb == null ? BigDecimal.ZERO : eb.getBalance());
        BigDecimal creditLimit = (eb == null ? BigDecimal.ZERO : eb.getCreditLimit());
        BigDecimal totalSpent = (eb == null ? BigDecimal.ZERO : eb.getTotalSpent());

        if (balance.add(creditLimit).compareTo(amount) < 0) {
            throw new RuntimeException("企业余额不足");
        }

        BigDecimal newBalance = balance.subtract(amount);
        BigDecimal newTotalSpent = totalSpent.add(amount);
        BigDecimal totalTopUp = (eb == null ? BigDecimal.ZERO : eb.getTotalTopUp());
        enterpriseBalanceMapper.upsert(companyId, newBalance, totalTopUp, newTotalSpent);

        EnterpriseBalanceTransaction txn = new EnterpriseBalanceTransaction();
        txn.setCompanyId(companyId);
        txn.setAmount(amount.negate());
        txn.setType("SETTLEMENT");
        txn.setRelatedBillId(relatedBillId);
        txn.setDescription(description);
        transactionMapper.insert(txn);
    }

    @Override
    @Transactional
    public void refund(Long companyId, BigDecimal amount, Long relatedBillId, String description) {
        EnterpriseBalance eb = enterpriseBalanceMapper.findByCompanyId(companyId);
        BigDecimal balance = (eb == null ? BigDecimal.ZERO : eb.getBalance());
        BigDecimal totalSpent = (eb == null ? BigDecimal.ZERO : eb.getTotalSpent());
        BigDecimal totalTopUp = (eb == null ? BigDecimal.ZERO : eb.getTotalTopUp());

        BigDecimal newBalance = balance.add(amount);
        BigDecimal newTotalSpent = totalSpent.subtract(amount);
        enterpriseBalanceMapper.upsert(companyId, newBalance, totalTopUp, newTotalSpent);

        EnterpriseBalanceTransaction txn = new EnterpriseBalanceTransaction();
        txn.setCompanyId(companyId);
        txn.setAmount(amount);
        txn.setType("SETTLEMENT_REFUND");
        txn.setRelatedBillId(relatedBillId);
        txn.setDescription(description);
        transactionMapper.insert(txn);
    }

    private synchronized String generateTopUpSerial() {
        String ts = LocalDateTime.now().format(SERIAL_FMT);
        int seq = RANDOM.nextInt(10000);
        return "TOP" + ts + String.format("%04d", seq);
    }
}
