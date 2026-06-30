package com.parttime.platform.service;

import java.util.List;
import java.util.Map;

public interface ReportService {
    Map<String, Object> overview();
    List<Map<String, Object>> enterpriseActivity();
    List<Map<String, Object>> workerActivity();
    Map<String, Object> supplyDemandAnalysis();
    Map<String, Object> conversionFunnel();
}
