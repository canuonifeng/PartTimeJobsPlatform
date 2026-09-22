package com.parttime.cservice.service;

import com.parttime.cservice.pojo.cmd.ExamSubmitCmd;
import com.parttime.cservice.pojo.cmd.StartLessonCmd;
import com.parttime.cservice.pojo.vo.ExamResultVO;
import com.parttime.cservice.pojo.vo.LessonStartVO;
import com.parttime.cservice.pojo.vo.TrainingCourseDetailVO;
import com.parttime.cservice.pojo.vo.TrainingCourseVO;
import com.parttime.cservice.pojo.vo.WorkerCertificationVO;

import java.util.List;

public interface TrainingService {

    List<TrainingCourseVO> listCourses(Long workerId);

    TrainingCourseDetailVO getCourseDetail(Long workerId, Long courseId);

    LessonStartVO startLesson(Long workerId, StartLessonCmd cmd);

    void startCourse(Long workerId, Long courseId);

    ExamResultVO submitExam(Long workerId, Long courseId, ExamSubmitCmd cmd);

    List<WorkerCertificationVO> getMyCertifications(Long workerId);
}
