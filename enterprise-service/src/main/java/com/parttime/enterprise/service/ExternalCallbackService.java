package com.parttime.enterprise.service;

import com.parttime.enterprise.pojo.cmd.AnnotationSubmitCmd;
import com.parttime.enterprise.pojo.cmd.ProgressCmd;
import com.parttime.enterprise.pojo.cmd.QualityCheckCmd;

public interface ExternalCallbackService {

    void handleAnnotationSubmit(AnnotationSubmitCmd cmd, String callbackKey);

    void handleQualityCheck(QualityCheckCmd cmd, String callbackKey);

    void handleProgress(ProgressCmd cmd, String callbackKey);
}
