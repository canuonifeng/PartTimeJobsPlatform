package com.parttime.platform.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ComplaintVO {
    private Long id;
    private String complaintNo;
    private String complainantType;
    private Long complainantId;
    private String complainantName;
    private String complainantPhone;
    private String accusedType;
    private Long accusedId;
    private String accusedName;
    private String complaintType;
    private String title;
    private String content;
    private String images;
    private Long relatedJobId;
    private String relatedJobTitle;
    private Long relatedScheduleId;
    private String status;
    private String priority;
    private String handlerName;
    private String handleResult;
    private LocalDateTime handledAt;
    private Integer satisfactionScore;
    private Boolean isAppeal;
    private Long parentId;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
