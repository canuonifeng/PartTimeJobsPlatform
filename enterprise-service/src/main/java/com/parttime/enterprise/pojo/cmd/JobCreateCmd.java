package com.parttime.enterprise.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import com.parttime.enterprise.enums.TaskType;
import com.parttime.enterprise.enums.PricingMode;

@Data
public class JobCreateCmd {

    @Schema(description = "企业ID")
    private Long companyId;
    @Schema(description = "岗位标题")
    private String title;
    @Schema(description = "岗位职责")
    private String description;
    @Schema(description = "任职要求")
    private String requirements;
    @Schema(description = "岗位联系人姓名")
    private String contactName;
    @Schema(description = "岗位联系方式")
    private String contactPhone;
    @Schema(description = "岗位标签ID列表")
    private List<Long> tagIds;
    @Schema(description = "工作地点")
    private String location;
    @Schema(description = "省")
    private String province;
    @Schema(description = "市")
    private String city;
    @Schema(description = "区")
    private String district;
    @Schema(description = "详细地址（街道门牌号）")
    private String address;
    @Schema(description = "纬度")
    private BigDecimal latitude;
    @Schema(description = "经度")
    private BigDecimal longitude;
    @Schema(description = "岗位分类ID")
    private Long categoryId;
    @Schema(description = "招聘人数")
    private Integer headcount;
    @Schema(description = "报名截止时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime deadline;
    @Schema(description = "薪资规则列表")
    private List<JobRateCmd> rates;
    @Schema(description = "排班列表")
    private List<JobScheduleCmd> schedules;
    @Schema(description = "岗位图片URL")
    private String imageUrl;
    @Schema(description = "任务类型: WORK-工作, ANNOTATION-标注")
    private TaskType taskType;
    @Schema(description = "计价模式: PER_ITEM-按件, PER_PACKAGE-按包")
    private PricingMode pricingMode;
    @Schema(description = "单价")
    private BigDecimal pricePerUnit;
    @Schema(description = "总数量")
    private Integer totalItems;
    @Schema(description = "外部任务ID")
    private String externalTaskId;
    @Schema(description = "外部系统类型")
    private String externalSystemType;
    @Schema(description = "是否急招")
    private Boolean urgent;
}
