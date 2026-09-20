package com.parttime.platform.pojo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OperationLog {

    @Schema(description = "日志ID")
    private Long id;
    @Schema(description = "操作人ID")
    private Long operatorId;
    @Schema(description = "操作人姓名")
    private String operatorName;
    @Schema(description = "模块")
    private String module;
    @Schema(description = "操作类型")
    private String operationType;
    @Schema(description = "目标类型")
    private String targetType;
    @Schema(description = "目标ID")
    private Long targetId;
    @Schema(description = "目标名称")
    private String targetName;
    @Schema(description = "IP地址")
    private String ipAddress;
    @Schema(description = "User Agent")
    private String userAgent;
    @Schema(description = "请求方法")
    private String requestMethod;
    @Schema(description = "请求URL")
    private String requestUrl;
    @Schema(description = "请求参数")
    private String requestParams;
    @Schema(description = "操作结果")
    private String result;
    @Schema(description = "错误信息")
    private String errorMessage;
    @Schema(description = "备注")
    private String remark;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
