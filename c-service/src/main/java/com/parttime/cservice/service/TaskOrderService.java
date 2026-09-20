package com.parttime.cservice.service;

import com.parttime.cservice.pojo.cmd.GrabTaskOrderCmd;
import com.parttime.cservice.pojo.vo.PageVO;
import com.parttime.cservice.pojo.vo.TaskOrderVO;

public interface TaskOrderService {
    PageVO<TaskOrderVO> getMyTaskOrders(Long workerId, int page, int pageSize);

    /**
     * 标注任务抢单：校验认证后为可用批次创建 PENDING 报名单。
     *
     * @return 成功抢到的批次数量
     */
    int grabTaskOrder(Long workerId, GrabTaskOrderCmd cmd);
}
