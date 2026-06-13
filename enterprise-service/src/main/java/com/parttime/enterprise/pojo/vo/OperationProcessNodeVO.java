package com.parttime.enterprise.pojo.vo;

import lombok.Data;
import java.util.List;

@Data
public class OperationProcessNodeVO {
    private String code;
    private String name;
    private Long count;
    private String status;
    private String description;
    private List<String> tags;
    private String routePath;
}
