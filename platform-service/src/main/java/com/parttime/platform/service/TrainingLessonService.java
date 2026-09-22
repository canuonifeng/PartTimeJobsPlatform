package com.parttime.platform.service;

import com.parttime.platform.pojo.cmd.TrainingLessonCreateCmd;
import com.parttime.platform.pojo.cmd.TrainingLessonQueryCmd;
import com.parttime.platform.pojo.cmd.TrainingLessonSortCmd;
import com.parttime.platform.pojo.cmd.TrainingLessonUpdateCmd;
import com.parttime.platform.pojo.vo.TrainingLessonVO;

import java.util.List;

public interface TrainingLessonService {

    List<TrainingLessonVO> list(TrainingLessonQueryCmd cmd);

    TrainingLessonVO create(TrainingLessonCreateCmd cmd);

    TrainingLessonVO update(TrainingLessonUpdateCmd cmd);

    void sort(TrainingLessonSortCmd cmd);

    void delete(Long id);

    void publish(Long id);

    void offline(Long id);
}
