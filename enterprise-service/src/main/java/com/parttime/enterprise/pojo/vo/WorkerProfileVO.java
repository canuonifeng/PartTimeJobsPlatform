package com.parttime.enterprise.pojo.vo;

import lombok.Data;
import java.util.List;

@Data
public class WorkerProfileVO {

    private Long workerId;
    private Double avgRating;
    private Integer totalEvaluations;
    private Boolean isBlacklisted;
    private String blacklistReason;
    private List<WorkHistoryVO> workHistory;
}
