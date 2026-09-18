package com.parttime.enterprise.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "创建外部工人映射请求")
public class ExternalWorkerMappingCreateCmd {

    @Schema(description = "工人ID", required = true)
    private Long workerId;

    @Schema(description = "外部系统类型", required = true)
    private String externalSystemType;

    @Schema(description = "外部系统工人ID", required = true)
    private String externalWorkerId;
}
