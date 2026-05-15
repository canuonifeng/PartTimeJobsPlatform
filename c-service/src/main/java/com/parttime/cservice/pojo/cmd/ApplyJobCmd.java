package com.parttime.cservice.pojo.cmd;

import java.util.List;

public record ApplyJobCmd(Long jobId, List<Long> scheduleIds) {
}
