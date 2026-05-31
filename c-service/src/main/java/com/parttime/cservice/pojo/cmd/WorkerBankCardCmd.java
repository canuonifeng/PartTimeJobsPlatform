package com.parttime.cservice.pojo.cmd;

import lombok.Data;

@Data
public class WorkerBankCardCmd {
    private String cardHolder;
    private String cardNumber;
    private String bankName;
    private String bankBranch;
}
