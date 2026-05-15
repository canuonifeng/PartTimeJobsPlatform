package com.parttime.enterprise.pojo.cmd;

import lombok.Data;

@Data
public class EvaluationCmd {

    private Long companyId;
    private Long jobId;
    private Integer rating;
    private String comment;
}
