package com.parttime.enterprise.service;

import com.parttime.enterprise.pojo.cmd.PayrollBatchCmd;
import com.parttime.enterprise.pojo.vo.PayrollBatchVO;
import com.parttime.enterprise.pojo.vo.PayrollItemVO;

import java.util.List;

public interface PayrollService {

    PayrollBatchVO createBatch(PayrollBatchCmd request);

    PayrollBatchVO calculateBatch(Long batchId);

    PayrollBatchVO confirmBatch(Long batchId);

    PayrollBatchVO payBatch(Long batchId);

    PayrollBatchVO getBatchById(Long batchId);

    List<PayrollBatchVO> getBatchesByCompany(Long companyId);

    List<PayrollItemVO> getBatchItems(Long batchId);
}
