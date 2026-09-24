package com.parttime.cservice.service;

import com.aliyun.oss.OSS;
import com.parttime.cservice.config.OssProperties;
import com.parttime.cservice.pojo.vo.SignedUrlVO;
import com.parttime.cservice.service.impl.OssSignedUrlServiceImpl;
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

    private static final Long WORKER_ID = 42L;
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

    private void mockPresignedUrl() throws MalformedURLException {
        when(ossClient.generatePresignedUrl(eq("linggong-private"), eq("realname/42/id.jpg"), any(Date.class)))
                .thenReturn(new URL("https://linggong-private.oss-cn-hangzhou.aliyuncs.com/realname/42/id.jpg?signature=abc"));
    }

    @Test
    void getSignedUrl_ownRealnameKey_shouldReturnUrl() throws Exception {
        mockPresignedUrl();

        long before = System.currentTimeMillis();
        SignedUrlVO vo = service.getSignedUrl(WORKER_ID, "realname/42/id.jpg");
        long after = System.currentTimeMillis();

        assertThat(vo.getUrl()).isEqualTo("https://linggong-private.oss-cn-hangzhou.aliyuncs.com/realname/42/id.jpg?signature=abc");
        long expire = EXPIRE_MILLIS;
        assertThat(vo.getExpiresAt()).isBetween(before + expire - 1000, after + expire + 1000);
        verify(ossClient).generatePresignedUrl(eq("linggong-private"), eq("realname/42/id.jpg"), any(Date.class));
    }

    @Test
    void getSignedUrl_otherWorkerKey_shouldThrowSecurity() {
        assertThatThrownBy(() -> service.getSignedUrl(WORKER_ID, "realname/999/id.jpg"))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("无权");
    }

    @Test
    void getSignedUrl_malformedKeys_shouldThrowIllegalArgument() {
        assertThatThrownBy(() -> service.getSignedUrl(WORKER_ID, ""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.getSignedUrl(WORKER_ID, "realname/"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.getSignedUrl(WORKER_ID, "realname/42"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.getSignedUrl(WORKER_ID, "realname/42/"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.getSignedUrl(WORKER_ID, "realname/42//id.jpg"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.getSignedUrl(WORKER_ID, "avatar/x.jpg"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.getSignedUrl(WORKER_ID, "../etc/passwd"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.getSignedUrl(WORKER_ID, "realname/42/a b.jpg"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.getSignedUrl(WORKER_ID, "realname/abc/id.jpg"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getSignedUrl_noOssClient_shouldThrowIllegalState() {
        when(ossClientProvider.getIfAvailable()).thenReturn(null);

        assertThatThrownBy(() -> service.getSignedUrl(WORKER_ID, "realname/42/id.jpg"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("OSS未配置");
    }
}
