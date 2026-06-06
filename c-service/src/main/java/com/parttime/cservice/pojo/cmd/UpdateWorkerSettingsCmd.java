package com.parttime.cservice.pojo.cmd;

import lombok.Data;

@Data
public class UpdateWorkerSettingsCmd {
    private Boolean pushEnabled;
    private Boolean locationEnabled;
    private Boolean quietEnabled;
}
