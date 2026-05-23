package com.parttime.enterprise.controller;

import com.parttime.enterprise.pojo.cmd.BlacklistCmd;
import com.parttime.enterprise.pojo.cmd.EvaluationCmd;
import com.parttime.enterprise.pojo.vo.EvaluationVO;
import com.parttime.enterprise.pojo.vo.WorkHistoryVO;
import com.parttime.enterprise.pojo.vo.WorkerProfileVO;
import com.parttime.enterprise.service.WorkerProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/enterprise/workers")
public class WorkerProfileController {

    @Resource
    private WorkerProfileService workerProfileService;

    @Operation(summary = "获取工人档案", description = "查看工人的详细信息、评价和工作记录")
    @GetMapping("/profile")
    public WorkerProfileVO getProfile(@Parameter(description = "工人ID") @RequestParam Long workerId,
                                      @Parameter(description = "企业ID") @RequestParam Long companyId) {
        return workerProfileService.getWorkerProfile(companyId, workerId);
    }

    @Operation(summary = "评价工人", description = "企业对工人进行评价打分")
    @PostMapping("/evaluations")
    @ResponseStatus(HttpStatus.CREATED)
    public EvaluationVO evaluateWorker(@Parameter(description = "工人ID") @RequestParam Long workerId,
                                        @RequestBody EvaluationCmd request) {
        return workerProfileService.evaluateWorker(
                request.getCompanyId(), request.getJobId(), workerId,
                request.getRating(), request.getComment());
    }

    @Operation(summary = "获取工人评价列表", description = "获取企业对该工人的所有评价")
    @GetMapping("/evaluations")
    public List<EvaluationVO> getEvaluations(@Parameter(description = "工人ID") @RequestParam Long workerId,
                                              @Parameter(description = "企业ID") @RequestParam Long companyId) {
        return workerProfileService.getEvaluations(workerId, companyId);
    }

    @Operation(summary = "拉黑工人", description = "将工人加入企业黑名单")
    @PostMapping("/blacklist")
    @ResponseStatus(HttpStatus.CREATED)
    public void addToBlacklist(@Parameter(description = "工人ID") @RequestParam Long workerId,
                                @RequestBody BlacklistCmd request) {
        workerProfileService.addToBlacklist(request.getCompanyId(), workerId, request.getReason());
    }

    @Operation(summary = "移除黑名单", description = "将工人从企业黑名单中移除")
    @DeleteMapping("/blacklist")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFromBlacklist(@Parameter(description = "工人ID") @RequestParam Long workerId,
                                    @Parameter(description = "企业ID") @RequestParam Long companyId) {
        workerProfileService.removeFromBlacklist(companyId, workerId);
    }

    @Operation(summary = "获取工人工作记录", description = "获取工人在该企业的工作历史")
    @GetMapping("/work-history")
    public List<WorkHistoryVO> getWorkHistory(@Parameter(description = "工人ID") @RequestParam Long workerId,
                                               @Parameter(description = "企业ID") @RequestParam Long companyId) {
        return workerProfileService.getWorkHistory(workerId, companyId);
    }
}
