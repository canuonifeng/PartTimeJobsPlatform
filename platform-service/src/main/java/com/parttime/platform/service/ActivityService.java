package com.parttime.platform.service;

import com.parttime.platform.pojo.cmd.ActivityCmd;
import com.parttime.platform.pojo.cmd.ActivityToggleCmd;
import com.parttime.platform.pojo.cmd.IdCmd;
import com.parttime.platform.pojo.cmd.PushTaskCmd;
import com.parttime.platform.pojo.vo.ActivityEffectVO;
import com.parttime.platform.pojo.vo.OperationActivityVO;
import com.parttime.platform.pojo.vo.PushTaskVO;

import java.util.List;

public interface ActivityService {

    List<OperationActivityVO> list();

    void create(ActivityCmd cmd);

    void update(ActivityCmd cmd);

    void toggle(ActivityToggleCmd cmd);

    void delete(IdCmd cmd);

    ActivityEffectVO effectStats(IdCmd cmd);

    List<PushTaskVO> pushTasks();

    void createPush(PushTaskCmd cmd);
}
