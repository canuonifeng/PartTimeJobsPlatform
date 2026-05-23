package com.parttime.enterprise.controller;

import com.parttime.enterprise.pojo.vo.WorkerListVO;
import com.parttime.enterprise.service.CompanyWorkerService;
import com.parttime.enterprise.config.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/enterprise/company-workers")
public class CompanyWorkerController {

    @Resource
    private CompanyWorkerService companyWorkerService;

    @Operation(summary = "获取企业兼职列表（人才库）")
    @PostMapping("/list")
    public List<WorkerListVO> list(@RequestBody(required = false) Map<String, String> body) {
        Long companyId = SecurityUtil.getCurrentCompanyId();
        String keyword = body != null ? body.get("keyword") : null;
        return companyWorkerService.list(companyId, keyword);
    }

    @Operation(summary = "获取兼职详情")
    @PostMapping("/detail")
    public WorkerListVO detail(@RequestBody Map<String, Long> body) {
        return companyWorkerService.detail(body.get("id"));
    }

    @Operation(summary = "拉黑兼职")
    @PostMapping("/blacklist")
    public void blacklist(@RequestBody Map<String, Long> body) {
        companyWorkerService.blacklist(body.get("id"));
    }

    @Operation(summary = "取消拉黑")
    @PostMapping("/unblacklist")
    public void unblacklist(@RequestBody Map<String, Long> body) {
        companyWorkerService.unblacklist(body.get("id"));
    }
}
