package com.parttime.cservice.pojo.vo;

import lombok.Data;

@Data
public class WorkerSettingsVO {
    private Boolean pushEnabled;
    private Boolean locationEnabled;
    private Boolean quietEnabled;

    public static WorkerSettingsVO defaults() {
        WorkerSettingsVO vo = new WorkerSettingsVO();
        vo.setPushEnabled(true);
        vo.setLocationEnabled(true);
        vo.setQuietEnabled(false);
        return vo;
    }
}
