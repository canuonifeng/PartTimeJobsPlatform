package com.parttime.platform.controller;

import com.parttime.platform.pojo.cmd.IdCmd;
import com.parttime.platform.pojo.cmd.JobTagCmd;
import com.parttime.platform.pojo.cmd.JobTagGroupCmd;
import com.parttime.platform.pojo.vo.ApiResponse;
import com.parttime.platform.pojo.vo.JobTagGroupVO;
import com.parttime.platform.pojo.vo.JobTagVO;
import com.parttime.platform.service.JobTagService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class JobTagController {

    @Resource
    private JobTagService jobTagService;

    @Operation(summary = "获取所有岗位标签", description = "获取平台所有岗位标签组及标签列表")
    @GetMapping("/job-tags")
    public ApiResponse<List<JobTagGroupVO>> getAllGroups() {
        return ApiResponse.success(jobTagService.getAllGroups());
    }

    @Operation(summary = "创建岗位标签组", description = "创建新的岗位标签组")
    @PostMapping("/job-tag-groups")
    public ApiResponse<JobTagGroupVO> createGroup(@RequestBody JobTagGroupCmd cmd) {
        return ApiResponse.success(jobTagService.createGroup(cmd));
    }

    @Operation(summary = "更新岗位标签组", description = "更新岗位标签组信息")
    @PostMapping("/job-tag-groups/update")
    public ApiResponse<JobTagGroupVO> updateGroup(@RequestBody JobTagGroupCmd cmd) {
        return ApiResponse.success(jobTagService.updateGroup(cmd.getId(), cmd));
    }

    @Operation(summary = "删除岗位标签组", description = "删除指定的岗位标签组")
    @PostMapping("/job-tag-groups/delete")
    public void deleteGroup(@RequestBody IdCmd cmd) {
        jobTagService.deleteGroup(cmd.getId());
    }

    @Operation(summary = "创建岗位标签", description = "创建新的岗位标签")
    @PostMapping("/job-tags")
    public ApiResponse<JobTagVO> createTag(@RequestBody JobTagCmd cmd) {
        return ApiResponse.success(jobTagService.createTag(cmd));
    }

    @Operation(summary = "更新岗位标签", description = "更新岗位标签信息")
    @PostMapping("/job-tags/update")
    public ApiResponse<JobTagVO> updateTag(@RequestBody JobTagCmd cmd) {
        return ApiResponse.success(jobTagService.updateTag(cmd.getId(), cmd));
    }

    @Operation(summary = "删除岗位标签", description = "删除指定的岗位标签")
    @PostMapping("/job-tags/delete")
    public void deleteTag(@RequestBody IdCmd cmd) {
        jobTagService.deleteTag(cmd.getId());
    }
}
