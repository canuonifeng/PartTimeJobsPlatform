package com.parttime.platform.service.impl;

import com.aliyun.oss.OSS;
import com.parttime.platform.config.OssProperties;
import com.parttime.platform.pojo.vo.SignedUrlVO;
import com.parttime.platform.service.OssSignedUrlService;
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

    public OssSignedUrlServiceImpl(OssProperties ossProperties,
                                   ObjectProvider<OSS> ossClientProvider) {
        this.ossProperties = ossProperties;
        this.ossClientProvider = ossClientProvider;
    }

    @Override
    public SignedUrlVO getSignedUrl(Long adminId, String key) {
        if (adminId == null) {
            throw new IllegalArgumentException("未登录");
        }
        if (!StringUtils.hasText(key)) {
            throw new IllegalArgumentException("key 不能为空");
        }
        if (key.contains("..") || key.contains("\\") || containsWhitespace(key)) {
            throw new IllegalArgumentException("非法 key");
        }

        String prefix;
        if (key.startsWith(REALNAME_PREFIX)) {
            prefix = REALNAME_PREFIX;
        } else if (key.startsWith(LICENSE_PREFIX)) {
            prefix = LICENSE_PREFIX;
        } else {
            throw new SecurityException("无权访问该文件");
        }

        String rest = key.substring(prefix.length());
        int slash = rest.indexOf('/');
        if (slash <= 0) {
            throw new IllegalArgumentException("非法 key");
        }
        String ownerPart = rest.substring(0, slash);
        String objectName = rest.substring(slash + 1);
        if (!isDigits(ownerPart)) {
            throw new IllegalArgumentException("非法 key");
        }
        if (!StringUtils.hasText(objectName) || objectName.startsWith("/")) {
            throw new IllegalArgumentException("非法 key");
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

    private boolean isDigits(String s) {
        if (!StringUtils.hasText(s)) {
            return false;
        }
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) {
                return false;
            }
        }
        return true;
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
