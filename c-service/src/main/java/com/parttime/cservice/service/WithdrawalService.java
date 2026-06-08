package com.parttime.cservice.service;

import com.parttime.cservice.pojo.vo.BankCardVO;
import com.parttime.cservice.pojo.vo.EarningsSummaryVO;
import com.parttime.cservice.pojo.vo.PageVO;
import com.parttime.cservice.pojo.vo.TransactionVO;
import com.parttime.cservice.pojo.vo.WithdrawalMethodVO;
import com.parttime.cservice.pojo.vo.WithdrawalVO;

import java.math.BigDecimal;
import java.util.List;

public interface WithdrawalService {

    /**
     * 申请提现
     * @param workerId 工人ID
     * @param amount 提现金额
     * @param withdrawalMethod 提现方式: WECHAT-微信零钱, BANK_CARD-银行卡
     * @param bankAccountId 银行卡ID（银行卡提现时需要）
     * @return 提现记录
     */
    WithdrawalVO requestWithdrawal(Long workerId, BigDecimal amount, String withdrawalMethod, Long bankAccountId);

    /**
     * 获取可用提现方式
     * @param workerId 工人ID
     * @return 可用提现方式列表
     */
    List<WithdrawalMethodVO> getAvailableMethods(Long workerId);

    /**
     * 获取银行卡列表
     * @param workerId 工人ID
     * @return 银行卡列表
     */
    List<BankCardVO> getBankCards(Long workerId);

    /**
     * 获取提现历史记录
     * @param workerId 工人ID
     * @return 提现记录列表
     */
    List<WithdrawalVO> getWithdrawalHistory(Long workerId);

    /**
     * 获取收益汇总
     * @param workerId 工人ID
     * @return 收益汇总
     */
    EarningsSummaryVO getEarningsSummary(Long workerId);

    /**
     * 获取账户流水
     * @param workerId 工人ID
     * @param page 页码
     * @param pageSize 每页大小
     * @return 流水分页
     */
    PageVO<TransactionVO> getTransactions(Long workerId, int page, int pageSize);
}
