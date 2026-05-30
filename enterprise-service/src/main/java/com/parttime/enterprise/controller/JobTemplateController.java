package com.parttime.enterprise.controller;

import com.parttime.enterprise.config.SecurityUtil;
import com.parttime.enterprise.pojo.cmd.TemplateCreateCmd;
import com.parttime.enterprise.pojo.cmd.TemplateUpdateCmd;
import com.parttime.enterprise.pojo.vo.JobTemplateVO;
import com.parttime.enterprise.service.JobTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/templates")
public class JobTemplateController {

    @Resource
    private JobTemplateService jobTemplateService;

    @Operation(summary = "获取模版列表")
    @PostMapping("/list")
    public ResponseEntity<List<JobTemplateVO>> list() {
        Long companyId = SecurityUtil.getCurrentCompanyId();
        return ResponseEntity.ok(jobTemplateService.list(companyId));
    }

    @Operation(summary = "新增模版")
    @PostMapping("/create")
    public ResponseEntity<JobTemplateVO> create(@RequestBody TemplateCreateCmd cmd) {
        Long companyId = SecurityUtil.getCurrentCompanyId();
        return ResponseEntity.ok(jobTemplateService.create(cmd, companyId));
    }

    @Operation(summary = "修改模版")
    @PostMapping("/update")
    public ResponseEntity<JobTemplateVO> update(@RequestBody TemplateUpdateCmd cmd) {
        return ResponseEntity.ok(jobTemplateService.update(cmd));
    }

    @Operation(summary = "删除模版")
    @PostMapping("/delete")
    public ResponseEntity<Void> delete(@RequestBody Map<String, Long> body) {
        jobTemplateService.delete(body.get("id"));
        return ResponseEntity.ok().build();
    }
}
