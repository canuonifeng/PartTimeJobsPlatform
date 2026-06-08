package com.parttime.cservice.service.impl;

import com.parttime.cservice.mapper.BalanceTransactionMapper;
import com.parttime.cservice.mapper.WithdrawalRecordMapper;
import com.parttime.cservice.mapper.WorkerBalanceMapper;
import com.parttime.cservice.mapper.WorkerBankCardMapper;
import com.parttime.cservice.mapper.WorkerRealNameAuthMapper;
import com.parttime.cservice.pojo.entity.BalanceTransaction;
import com.parttime.cservice.pojo.entity.WithdrawalRecord;
import com.parttime.cservice.pojo.entity.WorkerBalance;
import com.parttime.cservice.pojo.entity.WorkerBankCard;
import com.parttime.cservice.pojo.entity.WorkerRealNameAuth;
import com.parttime.cservice.pojo.vo.BankCardVO;
import com.parttime.cservice.pojo.vo.EarningsSummaryVO;
import com.parttime.cservice.pojo.vo.PageVO;
import com.parttime.cservice.pojo.vo.TransactionVO;
import com.parttime.cservice.pojo.vo.TransferResult;
import com.parttime.cservice.pojo.vo.WithdrawalMethodVO;
import com.parttime.cservice.pojo.vo.WithdrawalVO;
import com.parttime.cservice.service.NotificationService;
import com.parttime.cservice.service.WeChatPayService;
import com.parttime.cservice.service.WithdrawalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WithdrawalServiceImpl implements WithdrawalService {

    private static final Logger log = LoggerFactory.getLogger(WithdrawalServiceImpl.class);

    @Resource
    private WithdrawalRecordMapper withdrawalRecordMapper;

    @Resource
    private WorkerBalanceMapper workerBalanceMapper;

    @Resource
    private BalanceTransactionMapper balanceTransactionMapper;

    @Resource
    private WorkerRealNameAuthMapper workerRealNameAuthMapper;

    @Resource
    private WorkerBankCardMapper workerBankCardMapper;

    @Resource
    private NotificationService notificationService;

    @Resource
    private WeChatPayService weChatPayService;

    @Override
    @Transactional
    public WithdrawalVO requestWithdrawal(Long workerId, BigDecimal amount, String withdrawalMethod, Long bankAccountId) {
        log.info("开始处理提现请求: workerId={}, amount={}, method={}", workerId, amount, withdrawalMethod);
        
        // 验证提现金额
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("提现金额必须大于0");
        }
        if (amount.compareTo(BigDecimal.valueOf(1)) < 0) {
            throw new RuntimeException("最低提现金额为1元");
        }
        if (amount.compareTo(BigDecimal.valueOf(1000)) > 0) {
            throw new RuntimeException("最高提现金额为1000元");
        }

        // 验证实名认证
        Optional<WorkerRealNameAuth> auth = workerRealNameAuthMapper.findByWorkerId(workerId);
        if (auth.isEmpty() || !"APPROVED".equals(auth.get().getStatus())) {
            throw new RuntimeException("请先完成实名认证");
        }

        // 验证每日提现次数
        long todayCount = withdrawalRecordMapper.countTodayWithdrawals(workerId, LocalDate.now());
        if (todayCount >= 3) {
            throw new RuntimeException("今日提现次数已达上限（最多3次）");
        }

        // 验证银行卡（银行卡提现时）
        String openId = null;
        String bankAccount = null;
        String bankName = null;
        
        if ("BANK_CARD".equals(withdrawalMethod)) {
            if (bankAccountId == null) {
                throw new RuntimeException("请选择银行卡");
            }
            Optional<WorkerBankCard> bankCard = workerBankCardMapper.findByWorkerId(workerId);
            if (bankCard.isEmpty()) {
                throw new RuntimeException("请先绑定银行卡");
            }
            WorkerBankCard card = bankCard.get();
            bankAccount = card.getCardNumber();
            bankName = card.getBankName();
        } else if ("WECHAT".equals(withdrawalMethod)) {
            // 微信提现需要OpenID（实际项目中应该从微信登录获取）
            // 这里简化处理，使用工号作为OpenID
            openId = "o" + workerId;
        } else {
            throw new RuntimeException("不支持的提现方式");
        }

        // 验证余额
        WorkerBalance wb = workerBalanceMapper.findByWorkerId(workerId);
        if (wb == null || wb.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("余额不足");
        }

        // 创建提现记录
        WithdrawalRecord record = new WithdrawalRecord();
        record.setWorkerId(workerId);
        record.setAmount(amount);
        record.setStatus("PROCESSING");
        record.setWithdrawalMethod(withdrawalMethod);
        record.setBankAccount(bankAccount);
        record.setOpenId(openId);
        record.setRequestedAt(LocalDateTime.now());
        record.setProcessedAt(LocalDateTime.now());
        record.setCreatedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());
        withdrawalRecordMapper.insert(record);

        // 扣减余额
        workerBalanceMapper.upsert(workerId,
                wb.getBalance().subtract(amount),
                wb.getTotalEarned(),
                wb.getTotalWithdrawn().add(amount));

        // 创建余额变动记录
        BalanceTransaction bt = new BalanceTransaction();
        bt.setWorkerId(workerId);
        bt.setAmount(amount.negate());
        bt.setType("WITHDRAWAL");
        bt.setRelatedWithdrawalId(record.getId());
        bt.setDescription("提现支出: " + amount + "元");
        balanceTransactionMapper.insert(bt);

        // 调用微信支付API
        TransferResult transferResult;
        if ("WECHAT".equals(withdrawalMethod)) {
            transferResult = weChatPayService.transferToWechat(workerId, amount, openId, "提现到微信零钱");
        } else {
            transferResult = weChatPayService.transferToBankCard(workerId, amount, bankAccount, bankName, "提现到银行卡");
        }

        // 更新提现记录状态
        if (transferResult.isSuccess()) {
            withdrawalRecordMapper.updateCompletion(record.getId(), "COMPLETED",
                    transferResult.getTransferNo(), "WECHAT_PAY", LocalDateTime.now());
            
            // 发送成功通知
            notificationService.createWorkerNotification(workerId, "WITHDRAWAL_COMPLETED", "finance",
                    "提现成功", "您申请的提现" + amount + "元已到账", "WITHDRAWAL", record.getId());
            
            log.info("提现成功: workerId={}, amount={}, transferNo={}", workerId, amount, transferResult.getTransferNo());
        } else {
            // 转账失败，回滚余额
            workerBalanceMapper.upsert(workerId,
                    wb.getBalance(),
                    wb.getTotalEarned(),
                    wb.getTotalWithdrawn());
            
            withdrawalRecordMapper.updateCompletion(record.getId(), "FAILED",
                    transferResult.getTransferNo(), "WECHAT_PAY", LocalDateTime.now());
            
            // 删除余额变动记录
            balanceTransactionMapper.deleteByRelatedWithdrawalId(record.getId());
            
            // 发送失败通知
            notificationService.createWorkerNotification(workerId, "WITHDRAWAL_FAILED", "finance",
                    "提现失败", "您申请的提现" + amount + "元处理失败: " + transferResult.getErrorMessage(), "WITHDRAWAL", record.getId());
            
            log.error("提现失败: workerId={}, amount={}, error={}", workerId, amount, transferResult.getErrorMessage());
            throw new RuntimeException("提现失败: " + transferResult.getErrorMessage());
        }

        return toResponse(record);
    }

    @Override
    public List<WithdrawalMethodVO> getAvailableMethods(Long workerId) {
        List<WithdrawalMethodVO> methods = new ArrayList<>();
        
        // 微信零钱
        WithdrawalMethodVO wechat = new WithdrawalMethodVO();
        wechat.setCode("WECHAT");
        wechat.setName("微信零钱");
        wechat.setAvailable(true);
        wechat.setDescription("实时到账微信零钱");
        methods.add(wechat);
        
        // 银行卡
        WithdrawalMethodVO bankCard = new WithdrawalMethodVO();
        bankCard.setCode("BANK_CARD");
        bankCard.setName("银行卡");
        bankCard.setAvailable(true);
        bankCard.setDescription("实时到账银行卡");
        methods.add(bankCard);
        
        return methods;
    }

    @Override
    public List<BankCardVO> getBankCards(Long workerId) {
        List<BankCardVO> cards = new ArrayList<>();
        Optional<WorkerBankCard> bankCard = workerBankCardMapper.findByWorkerId(workerId);
        
        if (bankCard.isPresent()) {
            WorkerBankCard card = bankCard.get();
            BankCardVO vo = new BankCardVO();
            vo.setId(card.getId());
            vo.setBankName(card.getBankName());
            vo.setCardNumber(card.getCardNumber().substring(card.getCardNumber().length() - 4));
            vo.setCardHolder(card.getCardHolder());
            vo.setDefault(true);
            cards.add(vo);
        }
        
        return cards;
    }

    @Override
    public List<WithdrawalVO> getWithdrawalHistory(Long workerId) {
        return withdrawalRecordMapper.findByWorkerId(workerId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public EarningsSummaryVO getEarningsSummary(Long workerId) {
        WorkerBalance wb = workerBalanceMapper.findByWorkerId(workerId);

        EarningsSummaryVO resp = new EarningsSummaryVO();
        if (wb == null) {
            resp.setTotalEarned(BigDecimal.ZERO);
            resp.setTotalWithdrawn(BigDecimal.ZERO);
            resp.setPendingWithdrawal(BigDecimal.ZERO);
        } else {
            resp.setTotalEarned(wb.getTotalEarned());
            resp.setTotalWithdrawn(wb.getTotalWithdrawn());
            resp.setPendingWithdrawal(wb.getBalance());
        }
        return resp;
    }

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ZoneId CST = ZoneId.of("Asia/Shanghai");

    @Override
    public PageVO<TransactionVO> getTransactions(Long workerId, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<TransactionVO> list = balanceTransactionMapper.findByWorkerIdPage(workerId, offset, pageSize)
                .stream().map(t -> {
                    TransactionVO vo = new TransactionVO();
                    vo.setId(t.getId());
                    vo.setAmount(t.getAmount());
                    vo.setType(t.getType());
                    vo.setDescription(t.getDescription());
                    vo.setJobTitle(t.getJobTitle());
                    vo.setCompanyName(t.getCompanyName());
                    vo.setLocation(t.getLocation());
                    vo.setShiftDate(t.getShiftDate() == null ? null : t.getShiftDate().toString());
                    vo.setStartTime(t.getStartTime());
                    vo.setEndTime(t.getEndTime());
                    vo.setTotalHours(t.getTotalHours());
                    vo.setSettlementStatus(t.getSettlementStatus());
                    if (t.getCreatedAt() != null) {
                        vo.setCreatedAt(t.getCreatedAt().atZone(ZoneId.systemDefault())
                                .withZoneSameInstant(CST).format(FMT));
                    }
                    return vo;
                })
                .collect(Collectors.toList());
        long total = balanceTransactionMapper.countByWorkerId(workerId);
        return new PageVO<>(list, total);
    }

    private WithdrawalVO toResponse(WithdrawalRecord record) {
        WithdrawalVO resp = new WithdrawalVO();
        resp.setId(record.getId());
        resp.setWorkerId(record.getWorkerId());
        resp.setAmount(record.getAmount());
        resp.setStatus(record.getStatus());
        resp.setRequestedAt(record.getRequestedAt());
        resp.setThirdPartySerialNo(record.getThirdPartySerialNo());
        resp.setThirdPartyPlatform(record.getThirdPartyPlatform());
        resp.setCompletedAt(record.getCompletedAt());
        return resp;
    }
}
