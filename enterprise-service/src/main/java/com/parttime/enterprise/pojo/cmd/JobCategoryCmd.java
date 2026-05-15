package com.parttime.enterprise.pojo.cmd;

import lombok.Data;

@Data
public class JobCategoryCmd {

    private String name;
    private Long parentId;
    private Integer sortOrder;
}
