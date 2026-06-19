package com.parttime.cservice.pojo.vo;

import lombok.Data;

@Data
public class WxJsSdkConfigVO {
    private String appId;
    private long timestamp;
    private String nonceStr;
    private String signature;
    private String ghId;
}
