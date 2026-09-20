package com.parttime.platform.service;

import com.parttime.platform.pojo.cmd.TrainingCourseCmd;
import com.parttime.platform.pojo.vo.TrainingCourseVO;

import java.util.List;

public interface TrainingCourseService {

    List<TrainingCourseVO> list();

    TrainingCourseVO create(TrainingCourseCmd cmd);

    TrainingCourseVO update(TrainingCourseCmd cmd);

    void publish(Long id);

    void offline(Long id);

    void delete(Long id);
}
