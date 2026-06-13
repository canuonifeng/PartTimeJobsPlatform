package com.parttime.enterprise.pojo.vo;

import lombok.Data;
import java.util.List;

@Data
public class OperationTrendVO {
    private List<OperationTrendPointVO> days;
}
