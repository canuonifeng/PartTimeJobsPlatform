package com.parttime.enterprise.service;

import com.parttime.enterprise.pojo.cmd.TemplateCreateCmd;
import com.parttime.enterprise.pojo.cmd.TemplateUpdateCmd;
import com.parttime.enterprise.pojo.vo.JobTemplateVO;

import java.util.List;

public interface JobTemplateService {
    List<JobTemplateVO> list(Long companyId);
    JobTemplateVO create(TemplateCreateCmd cmd, Long companyId);
    JobTemplateVO update(TemplateUpdateCmd cmd);
    void delete(Long id);
}
