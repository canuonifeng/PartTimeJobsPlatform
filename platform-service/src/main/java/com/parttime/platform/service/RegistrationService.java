package com.parttime.platform.service;

import com.parttime.platform.pojo.cmd.ReviewRegistrationCmd;
import com.parttime.platform.pojo.vo.RegistrationListVO;
import com.parttime.platform.pojo.vo.RegistrationVO;

public interface RegistrationService {

    RegistrationListVO getRegistrations(String status);

    RegistrationVO getRegistration(Long id);

    RegistrationVO approveRegistration(Long id, String reviewerId);

    RegistrationVO rejectRegistration(Long id, String reviewerId, ReviewRegistrationCmd cmd);
}
