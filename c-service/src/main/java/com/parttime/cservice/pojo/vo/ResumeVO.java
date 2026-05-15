package com.parttime.cservice.pojo.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ResumeVO {

    private Long id;
    private Long workerId;
    private String fileName;
    private String fileUrl;
    private LocalDateTime uploadedAt;
}
