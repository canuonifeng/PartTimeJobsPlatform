package com.parttime.cservice.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConfigValueVO {
    private String key;
    private String value;
    private String updatedAt;
}
