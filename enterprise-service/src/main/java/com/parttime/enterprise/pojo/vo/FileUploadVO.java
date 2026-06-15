package com.parttime.enterprise.pojo.vo;

import lombok.Data;

@Data
public class FileUploadVO {
    private String url;
    private String fileName;
    private Long size;
    private String contentType;
}
