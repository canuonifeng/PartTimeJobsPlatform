package com.parttime.enterprise.pojo.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ScheduleTemplateVO {

    private Long id;
    private Long companyId;
    private String name;
    private String description;
    private List<ScheduleTemplateSlotVO> slots;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
