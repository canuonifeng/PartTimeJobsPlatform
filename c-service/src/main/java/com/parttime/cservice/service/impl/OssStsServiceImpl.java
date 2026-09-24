package com.parttime.cservice.service.impl;

import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.sts.model.v20150401.AssumeRoleRequest;
import com.aliyuncs.sts.model.v20150401.AssumeRoleResponse;
import com.parttime.cservice.config.OssProperties;
import com.parttime.cservice.pojo.vo.OssStsVO;
import com.parttime.cservice.service.OssStsService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

@Service
public class OssStsServiceImpl implements OssStsService {

    private static final String BIZ_REALNAME = "realname";
    private static final String BIZ_AVATAR = "avatar";

    private static final String STS_ACTIONS = String.join(",",
            "oss:PutObject",
            "oss:InitiateMultipartUpload",
            "oss:UploadPart",
            "oss:CompleteMultipartUpload",
            "oss:AbortMultipartUpload");

    private final OssProperties ossProperties;
    private final ObjectProvider<IAcsClient> stsClientProvider;

    public OssStsServiceImpl(OssProperties ossProperties, ObjectProvider<IAcsClient> stsClientProvider) {
        this.ossProperties = ossProperties;
        this.stsClientProvider = stsClientProvider;
    }

    @Override
    public OssStsVO issueSts(Long workerId, String biz) {
        if (workerId == null) {
            throw new IllegalArgumentException("未登录");
        }
        if (biz == null || biz.isBlank()) {
            throw new IllegalArgumentException("biz 不能为空");
        }

        String bucket;
        String prefix;
        if (BIZ_REALNAME.equals(biz)) {
            bucket = ossProperties.getPrivateBucket();
            prefix = BIZ_REALNAME + "/" + workerId + "/";
        } else if (BIZ_AVATAR.equals(biz)) {
            bucket = ossProperties.getPublicBucket();
            prefix = BIZ_AVATAR + "/" + workerId + "/";
        } else {
            throw new IllegalArgumentException("不支持的业务类型: " + biz);
        }

        IAcsClient stsClient = stsClientProvider.getIfAvailable();
        if (stsClient == null) {
            throw new IllegalStateException("OSS未配置");
        }

        String policy = buildPolicy(bucket, prefix);

        AssumeRoleRequest request = new AssumeRoleRequest();
        request.setRoleArn(ossProperties.getStsRoleArn());
        request.setRoleSessionName(ossProperties.getStsRoleSessionName() + "-worker-" + workerId);
        request.setPolicy(policy);

        AssumeRoleResponse response;
        try {
            response = stsClient.getAcsResponse(request);
        } catch (ClientException e) {
            throw new IllegalStateException("STS 调用失败: " + e.getMessage(), e);
        }

        AssumeRoleResponse.Credentials credentials = response.getCredentials();
        OssStsVO vo = new OssStsVO();
        vo.setBucket(bucket);
        vo.setEndpoint(ossProperties.getEndpoint());
        vo.setAccessKeyId(credentials.getAccessKeyId());
        vo.setAccessKeySecret(credentials.getAccessKeySecret());
        vo.setSecurityToken(credentials.getSecurityToken());
        vo.setExpiration(credentials.getExpiration());
        vo.setPrefix(prefix);
        return vo;
    }

    private String buildPolicy(String bucket, String prefix) {
        String resource = "acs:oss:*:*:" + bucket + "/" + prefix + "*";
        return "{\"Version\":\"1\","
                + "\"Statement\":[{"
                + "\"Effect\":\"Allow\","
                + "\"Action\":[" + quoteActions() + "],"
                + "\"Resource\":[\"" + resource + "\"]"
                + "]}]}";
    }

    private String quoteActions() {
        String[] actions = STS_ACTIONS.split(",");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < actions.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append("\"").append(actions[i]).append("\"");
        }
        return sb.toString();
    }
}
