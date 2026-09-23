package com.parttime.enterprise.service;

import com.parttime.enterprise.pojo.cmd.AnnotationBatchCreateCmd;
import com.parttime.enterprise.pojo.cmd.AnnotationBatchListCmd;
import com.parttime.enterprise.pojo.cmd.AnnotationBatchToggleCmd;
import com.parttime.enterprise.pojo.cmd.AnnotationBatchUpdateCmd;
import com.parttime.enterprise.pojo.vo.AnnotationBatchVO;

import java.util.List;

public interface AnnotationBatchService {

    List<AnnotationBatchVO> list(AnnotationBatchListCmd cmd);

    AnnotationBatchVO create(AnnotationBatchCreateCmd cmd);

    AnnotationBatchVO update(AnnotationBatchUpdateCmd cmd);

    AnnotationBatchVO toggle(AnnotationBatchToggleCmd cmd);
}
