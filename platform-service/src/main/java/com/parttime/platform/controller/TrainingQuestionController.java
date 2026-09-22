package com.parttime.platform.controller;

import com.parttime.platform.pojo.cmd.IdCmd;
import com.parttime.platform.pojo.cmd.QuestionCreateCmd;
import com.parttime.platform.pojo.cmd.QuestionListCmd;
import com.parttime.platform.pojo.cmd.QuestionUpdateCmd;
import com.parttime.platform.pojo.vo.ApiResponse;
import com.parttime.platform.pojo.vo.QuestionVO;
import com.parttime.platform.service.QuestionBankQuestionService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/admin/training/question-banks/questions")
public class TrainingQuestionController {

    @Resource
    private QuestionBankQuestionService questionBankQuestionService;

    @Operation(summary = "题目列表", description = "按题库ID查询题目列表")
    @PostMapping("/list")
    public ApiResponse<List<QuestionVO>> list(@RequestBody QuestionListCmd cmd) {
        return ApiResponse.success(questionBankQuestionService.list(cmd));
    }

    @Operation(summary = "创建题目", description = "创建新题目（草稿）")
    @PostMapping("/create")
    public ApiResponse<QuestionVO> create(@RequestBody QuestionCreateCmd cmd) {
        return ApiResponse.success(questionBankQuestionService.create(cmd));
    }

    @Operation(summary = "更新题目", description = "更新题目信息")
    @PostMapping("/update")
    public ApiResponse<QuestionVO> update(@RequestBody QuestionUpdateCmd cmd) {
        return ApiResponse.success(questionBankQuestionService.update(cmd));
    }

    @Operation(summary = "删除题目", description = "删除草稿状态的题目")
    @PostMapping("/delete")
    public ApiResponse<Void> delete(@RequestBody IdCmd cmd) {
        questionBankQuestionService.delete(cmd.getId());
        return ApiResponse.success();
    }

    @Operation(summary = "发布题目", description = "草稿题目发布为正式")
    @PostMapping("/publish")
    public ApiResponse<Void> publish(@RequestBody IdCmd cmd) {
        questionBankQuestionService.publish(cmd.getId());
        return ApiResponse.success();
    }

    @Operation(summary = "下线题目", description = "下线已发布题目")
    @PostMapping("/offline")
    public ApiResponse<Void> offline(@RequestBody IdCmd cmd) {
        questionBankQuestionService.offline(cmd.getId());
        return ApiResponse.success();
    }
}
