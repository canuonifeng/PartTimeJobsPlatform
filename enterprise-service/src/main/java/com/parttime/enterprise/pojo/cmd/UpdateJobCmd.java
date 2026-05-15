package com.parttime.enterprise.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UpdateJobCmd {

    @Schema(description = "企业ID")
    private Long companyId;
    @Schema(description = "岗位标题")
    private String title;
    @Schema(description = "岗位描述")
    private String description;
    @Schema(description = "工作地点")
    private String location;
    @Schema(description = "岗位分类ID")
    private Long categoryId;
    @Schema(description = "招聘人数")
    private Integer headcount;
    @Schema(description = "报名截止时间")
    private LocalDateTime deadline;
    @Schema(description = "薪资规则列表")
    private List<JobRateCmd> rates;
    @Schema(description = "排班列表")
    private List<JobScheduleCmd> schedules;
}
