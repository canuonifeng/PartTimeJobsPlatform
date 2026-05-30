package com.parttime.enterprise.controller;

import com.parttime.enterprise.config.SecurityUtil;
import com.parttime.enterprise.pojo.cmd.LocationCreateCmd;
import com.parttime.enterprise.pojo.cmd.LocationUpdateCmd;
import com.parttime.enterprise.pojo.vo.CompanyLocationVO;
import com.parttime.enterprise.service.CompanyLocationService;
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
@RequestMapping("/api/locations")
public class CompanyLocationController {

    @Resource
    private CompanyLocationService companyLocationService;

    @Operation(summary = "获取地点列表")
    @PostMapping("/list")
    public ResponseEntity<List<CompanyLocationVO>> list() {
        Long companyId = SecurityUtil.getCurrentCompanyId();
        return ResponseEntity.ok(companyLocationService.list(companyId));
    }

    @Operation(summary = "新增地点")
    @PostMapping("/create")
    public ResponseEntity<CompanyLocationVO> create(@RequestBody LocationCreateCmd cmd) {
        Long companyId = SecurityUtil.getCurrentCompanyId();
        return ResponseEntity.ok(companyLocationService.create(cmd, companyId));
    }

    @Operation(summary = "修改地点")
    @PostMapping("/update")
    public ResponseEntity<CompanyLocationVO> update(@RequestBody LocationUpdateCmd cmd) {
        return ResponseEntity.ok(companyLocationService.update(cmd));
    }

    @Operation(summary = "删除地点")
    @PostMapping("/delete")
    public ResponseEntity<Void> delete(@RequestBody Map<String, Long> body) {
        companyLocationService.delete(body.get("id"));
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "启用地点")
    @PostMapping("/enable")
    public ResponseEntity<Void> enable(@RequestBody Map<String, Long> body) {
        companyLocationService.enable(body.get("id"));
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "禁用地点")
    @PostMapping("/disable")
    public ResponseEntity<Void> disable(@RequestBody Map<String, Long> body) {
        companyLocationService.disable(body.get("id"));
        return ResponseEntity.ok().build();
    }
}
