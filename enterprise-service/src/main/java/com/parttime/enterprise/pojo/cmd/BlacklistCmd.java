package com.parttime.enterprise.pojo.cmd;

import lombok.Data;

@Data
public class BlacklistCmd {

    private Long companyId;
    private String reason;
}
