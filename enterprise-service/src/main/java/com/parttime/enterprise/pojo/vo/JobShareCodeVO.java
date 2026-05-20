package com.parttime.enterprise.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobShareCodeVO {

    private Long jobId;
    private String path;
    private String imageBase64;
}
