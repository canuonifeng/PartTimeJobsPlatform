package com.parttime.platform.service;

import com.aliyun.oss.OSS;
import com.parttime.platform.config.OssProperties;
import com.parttime.platform.pojo.vo.SignedUrlVO;
import com.parttime.platform.service.impl.OssSignedUrlServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OssSignedUrlServiceImplTest {

    private static final Long ADMIN_ID = 7L;
    private static final long EXPIRE_MILLIS = 600_000L;

    @Mock
    private OSS ossClient;

    @Mock
    private ObjectProvider<OSS> ossClientProvider;

    private OssProperties properties;

    private OssSignedUrlServiceImpl service;

    @BeforeEach
    void setUp() {
        properties = new OssProperties();
        properties.setEndpoint("oss-cn-hangzhou.aliyuncs.com");
        properties.setPrivateBucket("linggong-private");
        properties.setPublicBucket("linggong-public");

        lenient().when(ossClientProvider.getIfAvailable()).thenReturn(ossClient);
        service = new OssSignedUrlServiceImpl(properties, ossClientProvider);
    }

    private void mockPresignedUrl(String key) throws MalformedURLException {
        when(ossClient.generatePresignedUrl(eq("linggong-private"), eq(key), any(Date.class)))
                .thenReturn(new URL("https://linggong-private.oss-cn-hangzhou.aliyuncs.com/" + key + "?signature=abc"));
    }

    @Test
    void getSignedUrl_realnameKey_shouldReturnUrl() throws Exception {
        String key = "realname/42/id.jpg";
        mockPresignedUrl(key);

        long before = System.currentTimeMillis();
        SignedUrlVO vo = service.getSignedUrl(ADMIN_ID, key);
        long after = System.currentTimeMillis();

        assertThat(vo.getUrl()).isEqualTo("https://linggong-private.oss-cn-hangzhou.aliyuncs.com/" + key + "?signature=abc");
        assertThat(vo.getExpiresAt()).isBetween(before + EXPIRE_MILLIS - 1000, after + EXPIRE_MILLIS + 1000);
        verify(ossClient).generatePresignedUrl(eq("linggong-private"), eq(key), any(Date.class));
    }

    @Test
    void getSignedUrl_licenseKey_shouldReturnUrl() throws Exception {
        String key = "license/99/license.pdf";
        mockPresignedUrl(key);

        SignedUrlVO vo = service.getSignedUrl(ADMIN_ID, key);

        assertThat(vo.getUrl()).isEqualTo("https://linggong-private.oss-cn-hangzhou.aliyuncs.com/" + key + "?signature=abc");
        verify(ossClient).generatePresignedUrl(eq("linggong-private"), eq(key), any(Date.class));
    }

    @Test
    void getSignedUrl_malformedKeys_shouldThrowIllegalArgument() {
        assertThatThrownBy(() -> service.getSignedUrl(ADMIN_ID, ""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.getSignedUrl(ADMIN_ID, "realname/"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.getSignedUrl(ADMIN_ID, "realname/42"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.getSignedUrl(ADMIN_ID, "realname/42/"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.getSignedUrl(ADMIN_ID, "realname/42//id.jpg"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.getSignedUrl(ADMIN_ID, "../etc/passwd"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.getSignedUrl(ADMIN_ID, "realname/42/a b.jpg"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.getSignedUrl(ADMIN_ID, "realname\\42\\id.jpg"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.getSignedUrl(ADMIN_ID, "realname/abc/id.jpg"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getSignedUrl_publicPrefix_shouldThrowSecurity() {
        assertThatThrownBy(() -> service.getSignedUrl(ADMIN_ID, "job/logo/x.jpg"))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("无权");
        assertThatThrownBy(() -> service.getSignedUrl(ADMIN_ID, "avatar/1/head.jpg"))
                .isInstanceOf(SecurityException.class);
    }

    @Test
    void getSignedUrl_noOssClient_shouldThrowIllegalState() {
        when(ossClientProvider.getIfAvailable()).thenReturn(null);

        assertThatThrownBy(() -> service.getSignedUrl(ADMIN_ID, "realname/42/id.jpg"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("OSS未配置");
    }
}
