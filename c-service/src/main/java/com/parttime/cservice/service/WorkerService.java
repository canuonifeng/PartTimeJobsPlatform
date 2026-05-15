package com.parttime.cservice.service;

import com.parttime.cservice.pojo.cmd.LoginCmd;
import com.parttime.cservice.pojo.cmd.RegisterCmd;
import com.parttime.cservice.pojo.cmd.WeChatLoginCmd;
import com.parttime.cservice.pojo.vo.LoginVO;
import com.parttime.cservice.pojo.vo.WorkerVO;

public interface WorkerService {
    WorkerVO register(RegisterCmd request);
    String login(String wechatCode);
    LoginVO loginWithWechat(String code);
    WorkerVO getWorkerByOpenId(String openId);
    WorkerVO getWorkerById(Long id);
    WorkerVO updateProfile(Long id, RegisterCmd request);
}
