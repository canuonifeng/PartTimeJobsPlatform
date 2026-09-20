package com.parttime.platform.controller;

import com.parttime.platform.pojo.cmd.FaqQueryCmd;
import com.parttime.platform.pojo.cmd.FaqSaveCmd;
import com.parttime.platform.pojo.cmd.FaqSortCmd;
import com.parttime.platform.pojo.cmd.IdCmd;
import com.parttime.platform.pojo.vo.ApiResponse;
import com.parttime.platform.pojo.vo.FaqVO;
import com.parttime.platform.service.FaqService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/faqs")
public class FaqController {

    @Resource
    private FaqService faqService;

    @Operation(summary = "FAQ列表")
    @PostMapping("/list")
    public ApiResponse<List<FaqVO>> list(@RequestBody(required = false) FaqQueryCmd body) {
        return ApiResponse.success(faqService.list(body));
    }

    @Operation(summary = "FAQ详情")
    @PostMapping("/detail")
    public ApiResponse<FaqVO> detail(@RequestBody IdCmd body) {
        return ApiResponse.success(faqService.detail(body.getId()));
    }

    @Operation(summary = "新增FAQ")
    @PostMapping("/create")
    public ApiResponse<FaqVO> create(@RequestBody FaqSaveCmd body) {
        return ApiResponse.success(faqService.create(body));
    }

    @Operation(summary = "编辑FAQ")
    @PostMapping("/update")
    public ApiResponse<FaqVO> update(@RequestBody FaqSaveCmd body) {
        return ApiResponse.success(faqService.update(body));
    }

    @Operation(summary = "FAQ排序")
    @PostMapping("/sort")
    public ApiResponse<Void> sort(@RequestBody FaqSortCmd body) {
        faqService.sort(body);
        return ApiResponse.success();
    }

    @Operation(summary = "删除FAQ")
    @PostMapping("/delete")
    public ApiResponse<Void> delete(@RequestBody IdCmd body) {
        faqService.delete(body.getId());
        return ApiResponse.success();
    }
}
