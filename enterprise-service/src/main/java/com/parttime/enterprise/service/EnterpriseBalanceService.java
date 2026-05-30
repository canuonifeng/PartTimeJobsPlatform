package com.parttime.enterprise.service;

import com.parttime.enterprise.pojo.vo.EnterpriseBalanceVO;
import com.parttime.enterprise.pojo.vo.EnterpriseTransactionVO;
import com.parttime.enterprise.pojo.vo.PageVO;
import java.math.BigDecimal;

public interface EnterpriseBalanceService {

    EnterpriseBalanceVO getBalance(Long companyId);

    void topUp(Long companyId, BigDecimal amount);

    PageVO<EnterpriseTransactionVO> getTransactions(Long companyId, int page, int pageSize);

    void deduct(Long companyId, BigDecimal amount, Long relatedBillId, String description);

    void refund(Long companyId, BigDecimal amount, Long relatedBillId, String description);
}
