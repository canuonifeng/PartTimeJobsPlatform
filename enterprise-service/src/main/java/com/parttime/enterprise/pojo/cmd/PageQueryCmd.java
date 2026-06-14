package com.parttime.enterprise.pojo.cmd;

import lombok.Data;

@Data
public class PageQueryCmd {
    private Integer page;
    private Integer pageSize;
}
