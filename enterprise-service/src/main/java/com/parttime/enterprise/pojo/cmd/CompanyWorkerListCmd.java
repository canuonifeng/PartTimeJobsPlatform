package com.parttime.enterprise.pojo.cmd;

import lombok.Data;

@Data
public class CompanyWorkerListCmd {
    private String keyword;
    private Integer page;
    private Integer pageSize;
}
