package com.parttime.platform.controller;

import com.parttime.platform.pojo.cmd.JobTagCmd;
import com.parttime.platform.pojo.cmd.JobTagGroupCmd;
import com.parttime.platform.pojo.vo.JobTagGroupVO;
import com.parttime.platform.pojo.vo.JobTagVO;
import com.parttime.platform.service.JobTagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
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
    public List<JobTagGroupVO> getAllGroups() {
        return jobTagService.getAllGroups();
    }

    @Operation(summary = "创建岗位标签组", description = "创建新的岗位标签组")
    @PostMapping("/job-tag-groups")
    @ResponseStatus(HttpStatus.CREATED)
    public JobTagGroupVO createGroup(@RequestBody JobTagGroupCmd cmd) {
        return jobTagService.createGroup(cmd);
    }

    @Operation(summary = "更新岗位标签组", description = "更新岗位标签组信息")
    @PutMapping("/job-tag-groups")
    public JobTagGroupVO updateGroup(@Parameter(description = "标签组ID") @RequestParam Long id, @RequestBody JobTagGroupCmd cmd) {
        return jobTagService.updateGroup(id, cmd);
    }

    @Operation(summary = "删除岗位标签组", description = "删除指定的岗位标签组")
    @DeleteMapping("/job-tag-groups")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGroup(@Parameter(description = "标签组ID") @RequestParam Long id) {
        jobTagService.deleteGroup(id);
    }

    @Operation(summary = "创建岗位标签", description = "创建新的岗位标签")
    @PostMapping("/job-tags")
    @ResponseStatus(HttpStatus.CREATED)
    public JobTagVO createTag(@RequestBody JobTagCmd cmd) {
        return jobTagService.createTag(cmd);
    }

    @Operation(summary = "更新岗位标签", description = "更新岗位标签信息")
    @PutMapping("/job-tags")
    public JobTagVO updateTag(@Parameter(description = "标签ID") @RequestParam Long id, @RequestBody JobTagCmd cmd) {
        return jobTagService.updateTag(id, cmd);
    }

    @Operation(summary = "删除岗位标签", description = "删除指定的岗位标签")
    @DeleteMapping("/job-tags")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTag(@Parameter(description = "标签ID") @RequestParam Long id) {
        jobTagService.deleteTag(id);
    }
}
