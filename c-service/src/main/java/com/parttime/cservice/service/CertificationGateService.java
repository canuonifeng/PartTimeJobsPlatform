package com.parttime.cservice.service;

/**
 * 抢单认证校验服务：标注等任务类型抢单前校验兼职技能认证。
 */
public interface CertificationGateService {

    /**
     * 校验兼职是否持有指定任务类型对应的有效技能认证。
     *
     * @param workerId 工人ID
     * @param taskType 任务类型（如 ANNOTATION）
     * @return 校验通过返回 true；不通过返回 false
     */
    boolean hasCertification(Long workerId, String taskType);

    /**
     * 校验兼职是否持有指定任务类型对应的有效技能认证，不通过时抛出业务异常。
     *
     * @param workerId 工人ID
     * @param taskType 任务类型（如 ANNOTATION）
     */
    void checkCertification(Long workerId, String taskType);
}
