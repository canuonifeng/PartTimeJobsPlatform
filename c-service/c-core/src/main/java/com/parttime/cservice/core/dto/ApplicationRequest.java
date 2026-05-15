package com.parttime.cservice.core.dto;

import java.util.List;

public record ApplicationRequest(Long jobId, List<Long> scheduleIds) {
}
