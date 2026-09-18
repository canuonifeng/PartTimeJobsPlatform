package com.parttime.cservice.service;

import com.parttime.cservice.pojo.vo.PageVO;
import com.parttime.cservice.pojo.vo.TaskOrderVO;

public interface TaskOrderService {
    PageVO<TaskOrderVO> getMyTaskOrders(Long workerId, int page, int pageSize);
}
