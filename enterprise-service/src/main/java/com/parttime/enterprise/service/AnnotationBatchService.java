package com.parttime.enterprise.service;

import com.parttime.enterprise.pojo.cmd.AnnotationBatchCreateCmd;
import com.parttime.enterprise.pojo.cmd.AnnotationBatchListCmd;
import com.parttime.enterprise.pojo.cmd.AnnotationBatchToggleCmd;
import com.parttime.enterprise.pojo.cmd.AnnotationBatchUpdateCmd;
import com.parttime.enterprise.pojo.vo.AnnotationBatchVO;

import java.util.List;

public interface AnnotationBatchService {

    List<AnnotationBatchVO> list(Long companyId, AnnotationBatchListCmd cmd);

    AnnotationBatchVO create(Long companyId, AnnotationBatchCreateCmd cmd);

    AnnotationBatchVO update(Long companyId, AnnotationBatchUpdateCmd cmd);

    AnnotationBatchVO toggle(Long companyId, AnnotationBatchToggleCmd cmd);
}
