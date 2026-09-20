package com.parttime.platform.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "操作日志VO")
public class OperationLogVO {

    @Schema(description = "日志ID")
    private Long id;
    @Schema(description = "操作人")
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
    @Schema(description = "请求方法")
    private String requestMethod;
    @Schema(description = "请求URL")
    private String requestUrl;
    @Schema(description = "操作结果")
    private String result;
    @Schema(description = "错误信息")
    private String errorMessage;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
