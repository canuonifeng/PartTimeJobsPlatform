package com.parttime.enterprise.controller;

import com.parttime.enterprise.pojo.cmd.BlacklistCmd;
import com.parttime.enterprise.pojo.cmd.EvaluationCmd;
import com.parttime.enterprise.pojo.vo.EvaluationVO;
import com.parttime.enterprise.pojo.vo.WorkHistoryVO;
import com.parttime.enterprise.pojo.vo.WorkerProfileVO;
import com.parttime.enterprise.service.WorkerProfileService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/workers")
public class WorkerProfileController {

    @Resource
    private WorkerProfileService workerProfileService;

    @GetMapping("/profile")
    public WorkerProfileVO getProfile(@RequestParam Long workerId, @RequestParam Long companyId) {
        return workerProfileService.getWorkerProfile(companyId, workerId);
    }

    @PostMapping("/evaluations")
    @ResponseStatus(HttpStatus.CREATED)
    public EvaluationVO evaluateWorker(@RequestParam Long workerId, @RequestBody EvaluationCmd request) {
        return workerProfileService.evaluateWorker(
                request.getCompanyId(), request.getJobId(), workerId,
                request.getRating(), request.getComment());
    }

    @GetMapping("/evaluations")
    public List<EvaluationVO> getEvaluations(@RequestParam Long workerId, @RequestParam Long companyId) {
        return workerProfileService.getEvaluations(workerId, companyId);
    }

    @PostMapping("/blacklist")
    @ResponseStatus(HttpStatus.CREATED)
    public void addToBlacklist(@RequestParam Long workerId, @RequestBody BlacklistCmd request) {
        workerProfileService.addToBlacklist(request.getCompanyId(), workerId, request.getReason());
    }

    @DeleteMapping("/blacklist")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFromBlacklist(@RequestParam Long workerId, @RequestParam Long companyId) {
        workerProfileService.removeFromBlacklist(companyId, workerId);
    }

    @GetMapping("/work-history")
    public List<WorkHistoryVO> getWorkHistory(@RequestParam Long workerId, @RequestParam Long companyId) {
        return workerProfileService.getWorkHistory(workerId, companyId);
    }
}
