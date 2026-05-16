package com.parttime.platform.controller;

import com.parttime.platform.pojo.cmd.EnterpriseCreateCmd;
import com.parttime.platform.pojo.cmd.EnterpriseUpdateCmd;
import com.parttime.platform.pojo.vo.EnterpriseVO;
import com.parttime.platform.service.EnterpriseService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/enterprises")
public class EnterpriseController {

    @Resource
    private EnterpriseService enterpriseService;

    @Operation(summary = "获取企业列表")
    @PostMapping("/list")
    public List<EnterpriseVO> list(@RequestBody(required = false) Map<String, String> body) {
        String status = body != null ? body.get("status") : null;
        return enterpriseService.list(status);
    }

    @Operation(summary = "获取企业详情")
    @PostMapping("/detail")
    public EnterpriseVO detail(@RequestBody Map<String, Long> body) {
        return enterpriseService.detail(body.get("id"));
    }

    @Operation(summary = "新增企业")
    @PostMapping("/create")
    public EnterpriseVO create(@RequestBody EnterpriseCreateCmd cmd) {
        return enterpriseService.create(cmd);
    }

    @Operation(summary = "更新企业信息")
    @PostMapping("/update")
    public EnterpriseVO update(@RequestBody EnterpriseUpdateCmd cmd) {
        return enterpriseService.update(cmd);
    }

    @Operation(summary = "停用企业")
    @PostMapping("/suspend")
    public void suspend(@RequestBody Map<String, Long> body) {
        enterpriseService.suspend(body.get("id"));
    }

    @Operation(summary = "启用企业")
    @PostMapping("/activate")
    public void activate(@RequestBody Map<String, Long> body) {
        enterpriseService.activate(body.get("id"));
    }
}
