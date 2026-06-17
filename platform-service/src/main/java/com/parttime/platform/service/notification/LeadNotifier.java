package com.parttime.platform.service.notification;

import com.parttime.platform.pojo.entity.Lead;

public interface LeadNotifier {

    void notifyLeadCreated(Lead lead);
}
