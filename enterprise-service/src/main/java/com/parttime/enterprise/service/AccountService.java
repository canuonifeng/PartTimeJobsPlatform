package com.parttime.enterprise.service;

import com.parttime.enterprise.pojo.cmd.AccountCreateCmd;
import com.parttime.enterprise.pojo.cmd.AccountResetPasswordCmd;
import com.parttime.enterprise.pojo.cmd.AccountUpdateCmd;
import com.parttime.enterprise.pojo.vo.AccountVO;
import com.parttime.enterprise.pojo.vo.PageVO;

public interface AccountService {
    PageVO<AccountVO> list(Long enterpriseId, Integer page, Integer pageSize);
    AccountVO create(AccountCreateCmd cmd, Long enterpriseId);
    AccountVO update(AccountUpdateCmd cmd);
    void resetPassword(AccountResetPasswordCmd cmd);
    void delete(Long id);
}
