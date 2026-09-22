package com.parttime.platform.controller;

import com.parttime.platform.pojo.cmd.IdCmd;
import com.parttime.platform.pojo.cmd.QuestionBankCreateCmd;
import com.parttime.platform.pojo.cmd.QuestionBankUpdateCmd;
import com.parttime.platform.pojo.vo.ApiResponse;
import com.parttime.platform.pojo.vo.QuestionBankVO;
import com.parttime.platform.service.QuestionBankService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/admin/training/question-banks")
public class TrainingQuestionBankController {

    @Resource
    private QuestionBankService questionBankService;

    @Operation(summary = "题库列表", description = "获取所有题库，含题目数量")
    @PostMapping("/list")
    public ApiResponse<List<QuestionBankVO>> list() {
        return ApiResponse.success(questionBankService.list());
    }

    @Operation(summary = "创建题库", description = "创建新的题库，名称唯一")
    @PostMapping("/create")
    public ApiResponse<QuestionBankVO> create(@RequestBody QuestionBankCreateCmd cmd) {
        return ApiResponse.success(questionBankService.create(cmd));
    }

    @Operation(summary = "更新题库", description = "更新题库名称与描述")
    @PostMapping("/update")
    public ApiResponse<QuestionBankVO> update(@RequestBody QuestionBankUpdateCmd cmd) {
        return ApiResponse.success(questionBankService.update(cmd));
    }

    @Operation(summary = "启停题库", description = "切换题库启用与停用状态")
    @PostMapping("/toggle")
    public ApiResponse<Void> toggle(@RequestBody IdCmd cmd) {
        questionBankService.toggle(cmd.getId());
        return ApiResponse.success();
    }
}
