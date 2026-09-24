package com.parttime.enterprise.service;

import com.aliyun.oss.OSS;
import com.parttime.enterprise.config.OssProperties;
import com.parttime.enterprise.pojo.vo.SignedUrlVO;
import com.parttime.enterprise.service.impl.OssSignedUrlServiceImpl;
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

    private static final Long COMPANY_ID = 42L;
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
        when(ossClient.generatePresignedUrl(eq("linggong-private"), eq("license/42/license.pdf"), any(Date.class)))
                .thenReturn(new URL("https://linggong-private.oss-cn-hangzhou.aliyuncs.com/license/42/license.pdf?signature=abc"));
    }

    @Test
    void getSignedUrl_ownLicenseKey_shouldReturnUrl() throws Exception {
        mockPresignedUrl();

        long before = System.currentTimeMillis();
        SignedUrlVO vo = service.getSignedUrl(COMPANY_ID, "license/42/license.pdf");
        long after = System.currentTimeMillis();

        assertThat(vo.getUrl()).isEqualTo("https://linggong-private.oss-cn-hangzhou.aliyuncs.com/license/42/license.pdf?signature=abc");
        assertThat(vo.getExpiresAt()).isBetween(before + EXPIRE_MILLIS - 1000, after + EXPIRE_MILLIS + 1000);
        verify(ossClient).generatePresignedUrl(eq("linggong-private"), eq("license/42/license.pdf"), any(Date.class));
    }

    @Test
    void getSignedUrl_otherCompanyKey_shouldThrowSecurity() {
        assertThatThrownBy(() -> service.getSignedUrl(COMPANY_ID, "license/999/license.pdf"))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("无权");
    }

    @Test
    void getSignedUrl_realnameKey_shouldThrowSecurity() {
        assertThatThrownBy(() -> service.getSignedUrl(COMPANY_ID, "realname/42/id.jpg"))
                .isInstanceOf(SecurityException.class);
    }

    @Test
    void getSignedUrl_malformedKeys_shouldThrowIllegalArgument() {
        assertThatThrownBy(() -> service.getSignedUrl(COMPANY_ID, ""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.getSignedUrl(COMPANY_ID, "license/"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.getSignedUrl(COMPANY_ID, "license/42"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.getSignedUrl(COMPANY_ID, "license/42/"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.getSignedUrl(COMPANY_ID, "license/42//license.pdf"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.getSignedUrl(COMPANY_ID, "logo/42/logo.png"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.getSignedUrl(COMPANY_ID, "../etc/passwd"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.getSignedUrl(COMPANY_ID, "license/42/a b.pdf"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.getSignedUrl(COMPANY_ID, "license/abc/license.pdf"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getSignedUrl_noOssClient_shouldThrowIllegalState() {
        when(ossClientProvider.getIfAvailable()).thenReturn(null);

        assertThatThrownBy(() -> service.getSignedUrl(COMPANY_ID, "license/42/license.pdf"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("OSS未配置");
    }

    @Test
    void getSignedUrl_blankBucket_shouldThrowIllegalState() {
        properties.setPrivateBucket("");
        assertThatThrownBy(() -> service.getSignedUrl(COMPANY_ID, "license/42/license.pdf"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("OSS bucket 未配置");
    }

    @Test
    void getSignedUrl_nullCompanyId_shouldThrowIllegalState() {
        assertThatThrownBy(() -> service.getSignedUrl(null, "license/42/license.pdf"))
                .isInstanceOf(IllegalStateException.class);
    }
}
