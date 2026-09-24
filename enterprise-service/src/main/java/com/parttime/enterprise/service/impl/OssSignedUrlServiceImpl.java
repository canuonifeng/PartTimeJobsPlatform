package com.parttime.enterprise.service.impl;

import com.aliyun.oss.OSS;
import com.parttime.enterprise.config.OssProperties;
import com.parttime.enterprise.pojo.vo.SignedUrlVO;
import com.parttime.enterprise.service.OssSignedUrlService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URL;
import java.util.Date;

@Service
public class OssSignedUrlServiceImpl implements OssSignedUrlService {

    private static final String REALNAME_PREFIX = "realname/";
    private static final String LICENSE_PREFIX = "license/";
    private static final long EXPIRE_MILLIS = 600_000L;

    private final OssProperties ossProperties;
    private final ObjectProvider<OSS> ossClientProvider;

    public OssSignedUrlServiceImpl(OssProperties ossProperties, ObjectProvider<OSS> ossClientProvider) {
        this.ossProperties = ossProperties;
        this.ossClientProvider = ossClientProvider;
    }

    @Override
    public SignedUrlVO getSignedUrl(Long companyId, String key) {
        if (companyId == null) {
            throw new IllegalStateException("未登录");
        }
        if (!StringUtils.hasText(key)) {
            throw new IllegalArgumentException("key 不能为空");
        }
        if (key.contains("..") || key.contains("\\") || containsWhitespace(key)) {
            throw new IllegalArgumentException("非法 key");
        }

        if (key.startsWith(REALNAME_PREFIX)) {
            throw new SecurityException("企业端暂不支持访问该文件");
        }
        if (!key.startsWith(LICENSE_PREFIX)) {
            throw new IllegalArgumentException("非法 key");
        }

        String rest = key.substring(LICENSE_PREFIX.length());
        int slash = rest.indexOf('/');
        if (slash <= 0) {
            throw new IllegalArgumentException("非法 key");
        }
        String ownerPart = rest.substring(0, slash);
        String objectName = rest.substring(slash + 1);
        if (!StringUtils.hasText(objectName) || objectName.startsWith("/")) {
            throw new IllegalArgumentException("非法 key");
        }
        long ownerId;
        try {
            ownerId = Long.parseLong(ownerPart);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("非法 key");
        }
        if (ownerId != companyId) {
            throw new SecurityException("无权访问该文件");
        }

        String bucket = ossProperties.getPrivateBucket();
        if (!StringUtils.hasText(bucket)) {
            throw new IllegalStateException("OSS bucket 未配置");
        }

        OSS ossClient = ossClientProvider.getIfAvailable();
        if (ossClient == null) {
            throw new IllegalStateException("OSS未配置");
        }

        Date expiration = new Date(System.currentTimeMillis() + EXPIRE_MILLIS);
        URL url = ossClient.generatePresignedUrl(bucket, key, expiration);

        SignedUrlVO vo = new SignedUrlVO();
        vo.setUrl(url.toString());
        vo.setExpiresAt(expiration.getTime());
        return vo;
    }

    private boolean containsWhitespace(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (Character.isWhitespace(s.charAt(i))) {
                return true;
            }
        }
        return false;
    }
}
