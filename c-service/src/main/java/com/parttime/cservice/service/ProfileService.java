package com.parttime.cservice.service;

import com.parttime.cservice.pojo.cmd.ProfileUpdateCmd;
import com.parttime.cservice.pojo.vo.ProfileCompletenessVO;
import com.parttime.cservice.pojo.vo.ProfileVO;
import com.parttime.cservice.pojo.vo.ResumeVO;

import java.util.List;

public interface ProfileService {
    ProfileVO updateProfile(Long workerId, ProfileUpdateCmd request);
    ProfileVO getProfile(Long workerId);
    ProfileCompletenessVO getCompleteness(Long workerId);
    ResumeVO uploadResume(Long workerId, String fileName, String fileUrl);
    List<ResumeVO> getResumes(Long workerId);
}
