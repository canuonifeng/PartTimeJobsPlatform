package com.parttime.enterprise.controller;

import com.parttime.enterprise.pojo.cmd.CompanyWorkerIdCmd;
import com.parttime.enterprise.pojo.cmd.CompanyWorkerListCmd;
import com.parttime.enterprise.pojo.vo.ApiResponse;
import com.parttime.enterprise.pojo.vo.PageVO;
import com.parttime.enterprise.pojo.vo.WorkerListVO;
import com.parttime.enterprise.service.CompanyWorkerService;
import com.parttime.enterprise.config.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/enterprise/company-workers")
public class CompanyWorkerController {

    @Resource
    private CompanyWorkerService companyWorkerService;

    @Operation(summary = "获取企业兼职列表（人才库），分页返回")
    @PostMapping("/list")
    public ApiResponse<PageVO<WorkerListVO>> list(@RequestBody(required = false) CompanyWorkerListCmd cmd) {
        Long companyId = SecurityUtil.getCurrentCompanyId();
        String keyword = cmd != null ? cmd.getKeyword() : null;
        int page = cmd != null && cmd.getPage() != null ? cmd.getPage() : 1;
        int pageSize = cmd != null && cmd.getPageSize() != null ? cmd.getPageSize() : 20;
        return ApiResponse.success(companyWorkerService.list(companyId, keyword, page, pageSize));
    }

    @Operation(summary = "获取兼职详情")
    @PostMapping("/detail")
    public ApiResponse<WorkerListVO> detail(@RequestBody CompanyWorkerIdCmd cmd) {
        return ApiResponse.success(companyWorkerService.detail(cmd.getId()));
    }

    @Operation(summary = "拉黑兼职")
    @PostMapping("/blacklist")
    public void blacklist(@RequestBody CompanyWorkerIdCmd cmd) {
        companyWorkerService.blacklist(cmd.getId());
    }

    @Operation(summary = "取消拉黑")
    @PostMapping("/unblacklist")
    public void unblacklist(@RequestBody CompanyWorkerIdCmd cmd) {
        companyWorkerService.unblacklist(cmd.getId());
    }
}
