package com.parttime.cservice.service;

import com.parttime.cservice.pojo.vo.TransferResult;

import java.math.BigDecimal;

public interface WeChatPayService {

    /**
     * 商家转账到零钱
     * @param workerId 工人ID
     * @param amount 转账金额
     * @param openId 微信OpenID
     * @param description 转账描述
     * @return 转账结果
     */
    TransferResult transferToWechat(Long workerId, BigDecimal amount, String openId, String description);

    /**
     * 企业付款到银行卡
     * @param workerId 工人ID
     * @param amount 转账金额
     * @param bankAccount 银行账户
     * @param bankName 银行名称
     * @param description 转账描述
     * @return 转账结果
     */
    TransferResult transferToBankCard(Long workerId, BigDecimal amount, String bankAccount, String bankName, String description);

    /**
     * 查询转账状态
     * @param transferNo 转账流水号
     * @return 转账结果
     */
    TransferResult queryTransferStatus(String transferNo);
}
