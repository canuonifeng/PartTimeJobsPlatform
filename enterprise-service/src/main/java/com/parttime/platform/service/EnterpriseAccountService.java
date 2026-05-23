package com.parttime.platform.service;
import com.parttime.platform.pojo.cmd.EnterpriseAccountCreateCmd;
import com.parttime.platform.pojo.cmd.EnterpriseAccountUpdateCmd;
import com.parttime.platform.pojo.cmd.ResetPasswordCmd;
import com.parttime.platform.pojo.vo.EnterpriseAccountVO;
import java.util.List;

public interface EnterpriseAccountService {
    List<EnterpriseAccountVO> listByEnterprise(Long enterpriseId);
    EnterpriseAccountVO create(EnterpriseAccountCreateCmd cmd);
    EnterpriseAccountVO update(EnterpriseAccountUpdateCmd cmd);
    void resetPassword(ResetPasswordCmd cmd);
    void delete(Long id);
}
