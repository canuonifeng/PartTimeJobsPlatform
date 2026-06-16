package com.parttime.platform.service;

import com.parttime.platform.pojo.cmd.LeadCreateCmd;
import com.parttime.platform.pojo.vo.LeadVO;

public interface LeadService {

    LeadVO createLead(LeadCreateCmd cmd);
}
