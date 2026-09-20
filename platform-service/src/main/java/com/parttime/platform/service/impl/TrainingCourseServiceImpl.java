package com.parttime.platform.service.impl;

import com.parttime.platform.exception.BusinessException;
import com.parttime.platform.mapper.TrainingCertificationMapper;
import com.parttime.platform.mapper.TrainingCourseMapper;
import com.parttime.platform.pojo.cmd.TrainingCourseCmd;
import com.parttime.platform.pojo.entity.TrainingCertification;
import com.parttime.platform.pojo.entity.TrainingCourse;
import com.parttime.platform.pojo.vo.TrainingCourseVO;
import com.parttime.platform.service.TrainingCourseService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TrainingCourseServiceImpl implements TrainingCourseService {

    private static final String DRAFT = "DRAFT";
    private static final String PUBLISHED = "PUBLISHED";
    private static final String OFFLINE = "OFFLINE";

    @Resource
    private TrainingCourseMapper trainingCourseMapper;

    @Resource
    private TrainingCertificationMapper trainingCertificationMapper;

    @Override
    public List<TrainingCourseVO> list() {
        Map<Long, String> certNames = trainingCertificationMapper.findAll().stream()
                .collect(Collectors.toMap(TrainingCertification::getId, TrainingCertification::getName, (a, b) -> a));
        return trainingCourseMapper.findAll().stream()
                .map(course -> toVO(course, certNames.get(course.getCertificationId())))
                .toList();
    }

    @Override
    public TrainingCourseVO create(TrainingCourseCmd cmd) {
        validate(cmd);
        TrainingCourse course = new TrainingCourse();
        course.setCertificationId(cmd.getCertificationId());
        course.setTitle(cmd.getTitle().trim());
        course.setSummary(cmd.getSummary());
        course.setContent(cmd.getContent());
        course.setExamJson(cmd.getExamJson());
        course.setPassScore(cmd.getPassScore() == null ? 60 : cmd.getPassScore());
        course.setStatus(DRAFT);
        course.setSortOrder(cmd.getSortOrder() == null ? 0 : cmd.getSortOrder());
        trainingCourseMapper.insert(course);
        TrainingCertification certification = trainingCertificationMapper.findById(course.getCertificationId()).orElse(null);
        return toVO(course, certification == null ? null : certification.getName());
    }

    @Override
    public TrainingCourseVO update(TrainingCourseCmd cmd) {
        TrainingCourse course = trainingCourseMapper.findById(cmd.getId())
                .orElseThrow(() -> new BusinessException("课程不存在: " + cmd.getId()));
        if (cmd.getCertificationId() != null) {
            course.setCertificationId(cmd.getCertificationId());
        }
        if (cmd.getTitle() != null && !cmd.getTitle().isBlank()) {
            course.setTitle(cmd.getTitle().trim());
        }
        course.setSummary(cmd.getSummary());
        course.setContent(cmd.getContent());
        course.setExamJson(cmd.getExamJson());
        course.setPassScore(cmd.getPassScore() == null ? course.getPassScore() : cmd.getPassScore());
        course.setSortOrder(cmd.getSortOrder() == null ? course.getSortOrder() : cmd.getSortOrder());
        trainingCourseMapper.update(course);
        TrainingCertification certification = trainingCertificationMapper.findById(course.getCertificationId()).orElse(null);
        return toVO(course, certification == null ? null : certification.getName());
    }

    @Override
    public void publish(Long id) {
        TrainingCourse course = trainingCourseMapper.findById(id)
                .orElseThrow(() -> new BusinessException("课程不存在: " + id));
        trainingCertificationMapper.findById(course.getCertificationId())
                .orElseThrow(() -> new BusinessException("关联认证不存在，无法发布"));
        trainingCourseMapper.updateStatus(id, PUBLISHED);
    }

    @Override
    public void offline(Long id) {
        trainingCourseMapper.findById(id)
                .orElseThrow(() -> new BusinessException("课程不存在: " + id));
        trainingCourseMapper.updateStatus(id, OFFLINE);
    }

    @Override
    public void delete(Long id) {
        TrainingCourse course = trainingCourseMapper.findById(id)
                .orElseThrow(() -> new BusinessException("课程不存在: " + id));
        if (PUBLISHED.equals(course.getStatus())) {
            throw new BusinessException("已发布课程不能删除，请先下线");
        }
        trainingCourseMapper.deleteById(id);
    }

    private void validate(TrainingCourseCmd cmd) {
        if (cmd.getCertificationId() == null) {
            throw new BusinessException("请选择关联认证");
        }
        if (cmd.getTitle() == null || cmd.getTitle().isBlank()) {
            throw new BusinessException("课程标题不能为空");
        }
        trainingCertificationMapper.findById(cmd.getCertificationId())
                .orElseThrow(() -> new BusinessException("关联认证不存在: " + cmd.getCertificationId()));
    }

    private TrainingCourseVO toVO(TrainingCourse course, String certificationName) {
        TrainingCourseVO vo = new TrainingCourseVO();
        vo.setId(course.getId());
        vo.setCertificationId(course.getCertificationId());
        vo.setCertificationName(certificationName);
        vo.setTitle(course.getTitle());
        vo.setSummary(course.getSummary());
        vo.setPassScore(course.getPassScore());
        vo.setStatus(course.getStatus());
        vo.setSortOrder(course.getSortOrder());
        vo.setCreatedAt(course.getCreatedAt());
        vo.setUpdatedAt(course.getUpdatedAt());
        return vo;
    }
}
