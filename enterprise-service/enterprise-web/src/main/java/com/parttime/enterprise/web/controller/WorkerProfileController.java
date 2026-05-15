package com.parttime.enterprise.web.controller;

import com.parttime.enterprise.api.dto.*;
import com.parttime.enterprise.core.service.WorkerProfileService;

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
    public WorkerProfileResponse getProfile(@PathVariable Long workerId, @RequestParam Long companyId) {
        return workerProfileService.getWorkerProfile(companyId, workerId);
    }

    @PostMapping("/{workerId}/evaluations")
    @ResponseStatus(HttpStatus.CREATED)
    public EvaluationResponse evaluateWorker(@PathVariable Long workerId, @RequestBody EvaluationRequest request) {
        return workerProfileService.evaluateWorker(
                request.getCompanyId(), request.getJobId(), workerId,
                request.getRating(), request.getComment());
    }

    @GetMapping("/{workerId}/evaluations")
    public List<EvaluationResponse> getEvaluations(@PathVariable Long workerId, @RequestParam Long companyId) {
        return workerProfileService.getEvaluations(workerId, companyId);
    }

    @PostMapping("/{workerId}/blacklist")
    @ResponseStatus(HttpStatus.CREATED)
    public void addToBlacklist(@PathVariable Long workerId, @RequestBody BlacklistRequest request) {
        workerProfileService.addToBlacklist(request.getCompanyId(), workerId, request.getReason());
    }

    @DeleteMapping("/{workerId}/blacklist")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFromBlacklist(@PathVariable Long workerId, @RequestParam Long companyId) {
        workerProfileService.removeFromBlacklist(companyId, workerId);
    }

    @GetMapping("/{workerId}/work-history")
    public List<WorkHistoryResponse> getWorkHistory(@PathVariable Long workerId, @RequestParam Long companyId) {
        return workerProfileService.getWorkHistory(workerId, companyId);
    }
}
