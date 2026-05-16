package com.parttime.platform.service;
import com.parttime.platform.pojo.cmd.EnterpriseCreateCmd;
import com.parttime.platform.pojo.cmd.EnterpriseUpdateCmd;
import com.parttime.platform.pojo.vo.EnterpriseVO;
import java.util.List;

public interface EnterpriseService {
    List<EnterpriseVO> list(String status);
    EnterpriseVO detail(Long id);
    EnterpriseVO create(EnterpriseCreateCmd cmd);
    EnterpriseVO update(EnterpriseUpdateCmd cmd);
    void suspend(Long id);
    void activate(Long id);
}
