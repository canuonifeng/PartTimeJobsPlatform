package com.parttime.platform.service;

import com.parttime.platform.pojo.cmd.IdCmd;
import com.parttime.platform.pojo.cmd.OperatorCreateCmd;
import com.parttime.platform.pojo.cmd.OperatorUpdateCmd;
import com.parttime.platform.pojo.vo.OperatorVO;
import com.parttime.platform.pojo.vo.RoleVO;

import java.util.List;

public interface OperatorService {

    List<OperatorVO> list();

    void create(OperatorCreateCmd cmd);

    void update(OperatorUpdateCmd cmd);

    void resetPassword(IdCmd cmd);

    void toggleStatus(IdCmd cmd);

    List<RoleVO> roles();
}
