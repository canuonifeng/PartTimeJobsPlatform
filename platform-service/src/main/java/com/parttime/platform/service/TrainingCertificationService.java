package com.parttime.platform.service;

import com.parttime.platform.pojo.cmd.TrainingCertificationCmd;
import com.parttime.platform.pojo.vo.TrainingCertificationVO;

import java.util.List;

public interface TrainingCertificationService {

    List<TrainingCertificationVO> list();

    TrainingCertificationVO create(TrainingCertificationCmd cmd);

    TrainingCertificationVO update(TrainingCertificationCmd cmd);

    void toggle(Long id, String status);
}
