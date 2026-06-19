package com.parttime.cservice.pojo.vo;

import lombok.Data;

@Data
public class InviteInfoVO {
    private String inviterName;
    private String inviterAvatar;
    private String code;
    private String miniProgramScheme;
    private String miniProgramUrlLink;
}
