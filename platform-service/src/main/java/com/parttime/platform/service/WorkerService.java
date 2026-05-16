package com.parttime.platform.service;
import com.parttime.platform.pojo.cmd.WorkerUpdateCmd;
import com.parttime.platform.pojo.vo.WorkerVO;
import java.util.List;

public interface WorkerService {
    List<WorkerVO> list(String status, String keyword);
    WorkerVO detail(Long id);
    WorkerVO update(WorkerUpdateCmd cmd);
    void ban(Long id);
    void unban(Long id);
}
