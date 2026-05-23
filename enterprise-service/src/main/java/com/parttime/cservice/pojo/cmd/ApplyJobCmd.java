package com.parttime.cservice.pojo.cmd;

import java.util.List;

public record ApplyJobCmd(
        @io.swagger.v3.oas.annotations.media.Schema(description = "岗位ID") Long jobId,
        @io.swagger.v3.oas.annotations.media.Schema(description = "申请的排班ID列表") java.util.List<Long> scheduleIds) {
}
