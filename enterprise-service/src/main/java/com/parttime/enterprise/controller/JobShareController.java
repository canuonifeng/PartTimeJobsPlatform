package com.parttime.enterprise.controller;

import com.parttime.enterprise.pojo.vo.JobShareCodeVO;
import com.parttime.enterprise.pojo.vo.JobShareLinkVO;
import com.parttime.enterprise.service.JobShareService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/enterprise/jobs")
public class JobShareController {

    private final JobShareService jobShareService;

    @Autowired
    public JobShareController(JobShareService jobShareService) {
        this.jobShareService = jobShareService;
    }

    @Operation(summary = "生成职位分享二维码", description = "生成可供工人端扫码打开的职位小程序码")
    @GetMapping("/share-code")
    public JobShareCodeVO getShareCode(@Parameter(description = "职位ID") @RequestParam Long id) {
        return jobShareService.getShareCode(id);
    }

    @Operation(summary = "生成职位分享链接", description = "生成可复制到微信群的 C 端小程序 URL Scheme")
    @GetMapping("/share-link")
    public JobShareLinkVO getShareLink(@Parameter(description = "职位ID") @RequestParam Long id) {
        return jobShareService.getShareLink(id);
    }
}
