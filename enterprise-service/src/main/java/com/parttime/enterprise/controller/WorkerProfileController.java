package com.parttime.enterprise.controller;

import com.parttime.enterprise.pojo.cmd.BlacklistCmd;
import com.parttime.enterprise.pojo.cmd.EvaluationCmd;
import com.parttime.enterprise.pojo.vo.EvaluationVO;
import com.parttime.enterprise.pojo.vo.WorkHistoryVO;
import com.parttime.enterprise.pojo.vo.WorkerProfileVO;
import com.parttime.enterprise.service.WorkerProfileService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workers")
public class WorkerProfileController {

    private final WorkerProfileService workerProfileService;

    public WorkerProfileController(WorkerProfileService workerProfileService) {
        this.workerProfileService = workerProfileService;
    }

    @GetMapping("/{workerId}/profile")
    public WorkerProfileVO getProfile(@PathVariable Long workerId, @RequestParam Long companyId) {
        return workerProfileService.getWorkerProfile(companyId, workerId);
    }

    @PostMapping("/{workerId}/evaluations")
    @ResponseStatus(HttpStatus.CREATED)
    public EvaluationVO evaluateWorker(@PathVariable Long workerId, @RequestBody EvaluationCmd request) {
        return workerProfileService.evaluateWorker(
                request.getCompanyId(), request.getJobId(), workerId,
                request.getRating(), request.getComment());
    }

    @GetMapping("/{workerId}/evaluations")
    public List<EvaluationVO> getEvaluations(@PathVariable Long workerId, @RequestParam Long companyId) {
        return workerProfileService.getEvaluations(workerId, companyId);
    }

    @PostMapping("/{workerId}/blacklist")
    @ResponseStatus(HttpStatus.CREATED)
    public void addToBlacklist(@PathVariable Long workerId, @RequestBody BlacklistCmd request) {
        workerProfileService.addToBlacklist(request.getCompanyId(), workerId, request.getReason());
    }

    @DeleteMapping("/{workerId}/blacklist")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFromBlacklist(@PathVariable Long workerId, @RequestParam Long companyId) {
        workerProfileService.removeFromBlacklist(companyId, workerId);
    }

    @GetMapping("/{workerId}/work-history")
    public List<WorkHistoryVO> getWorkHistory(@PathVariable Long workerId, @RequestParam Long companyId) {
        return workerProfileService.getWorkHistory(workerId, companyId);
    }
}
