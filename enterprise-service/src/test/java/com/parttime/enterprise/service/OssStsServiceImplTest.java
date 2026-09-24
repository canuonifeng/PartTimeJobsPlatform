package com.parttime.enterprise.service;

import com.aliyuncs.IAcsClient;
import com.aliyuncs.sts.model.v20150401.AssumeRoleRequest;
import com.aliyuncs.sts.model.v20150401.AssumeRoleResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.parttime.enterprise.config.OssProperties;
import com.parttime.enterprise.pojo.vo.OssStsVO;
import com.parttime.enterprise.service.impl.OssStsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OssStsServiceImplTest {

    private static final Long COMPANY_ID = 42L;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private IAcsClient stsClient;

    @Mock
    private ObjectProvider<IAcsClient> stsClientProvider;

    private OssProperties properties;

    private OssStsServiceImpl service;

    @BeforeEach
    void setUp() {
        properties = new OssProperties();
        properties.setEndpoint("oss-cn-hangzhou.aliyuncs.com");
        properties.setRegion("cn-hangzhou");
        properties.setPrivateBucket("linggong-private");
        properties.setPublicBucket("linggong-public");
        properties.setStsRoleArn("acs:ram::1234:role/linggong-oss");
        properties.setStsRoleSessionName("linggong-enterprise");

        lenient().when(stsClientProvider.getIfAvailable()).thenReturn(stsClient);
        service = new OssStsServiceImpl(properties, stsClientProvider, objectMapper);
    }

    private void mockStsResponse() throws Exception {
        AssumeRoleResponse response = new AssumeRoleResponse();
        AssumeRoleResponse.Credentials credentials = new AssumeRoleResponse.Credentials();
        credentials.setAccessKeyId("LTAI-test");
        credentials.setAccessKeySecret("secret-test");
        credentials.setSecurityToken("token-test");
        credentials.setExpiration("2026-09-24T12:00:00Z");
        response.setCredentials(credentials);
        when(stsClient.getAcsResponse(org.mockito.ArgumentMatchers.<AssumeRoleRequest>any()))
                .thenReturn(response);
    }

    @Test
    void issueSts_license_shouldUsePrivateBucketAndPrefix() throws Exception {
        mockStsResponse();
        ArgumentCaptor<AssumeRoleRequest> captor = ArgumentCaptor.forClass(AssumeRoleRequest.class);

        OssStsVO vo = service.issueSts(COMPANY_ID, "license");

        assertThat(vo.getBucket()).isEqualTo("linggong-private");
        assertThat(vo.getPrefix()).isEqualTo("license/42/");
        assertThat(vo.getEndpoint()).isEqualTo("oss-cn-hangzhou.aliyuncs.com");
        assertThat(vo.getAccessKeyId()).isEqualTo("LTAI-test");
        assertThat(vo.getAccessKeySecret()).isEqualTo("secret-test");
        assertThat(vo.getSecurityToken()).isEqualTo("token-test");
        assertThat(vo.getExpiration()).isEqualTo("2026-09-24T12:00:00Z");

        verify(stsClient).getAcsResponse(captor.capture());
        AssumeRoleRequest req = captor.getValue();
        assertThat(req.getRoleArn()).isEqualTo("acs:ram::1234:role/linggong-oss");
        assertThat(req.getRoleSessionName()).isEqualTo("linggong-enterprise-enterprise-42");
        assertPolicy(req.getPolicy(), "linggong-private", "license/42/");
    }

    @Test
    void issueSts_logo_shouldUsePublicBucketAndPrefix() throws Exception {
        mockStsResponse();
        ArgumentCaptor<AssumeRoleRequest> captor = ArgumentCaptor.forClass(AssumeRoleRequest.class);

        OssStsVO vo = service.issueSts(COMPANY_ID, "logo");

        assertThat(vo.getBucket()).isEqualTo("linggong-public");
        assertThat(vo.getPrefix()).isEqualTo("logo/42/");

        verify(stsClient).getAcsResponse(captor.capture());
        AssumeRoleRequest req = captor.getValue();
        assertThat(req.getRoleSessionName()).isEqualTo("linggong-enterprise-enterprise-42");
        assertPolicy(req.getPolicy(), "linggong-public", "logo/42/");
    }

    @Test
    void issueSts_job_shouldUsePublicBucketAndPrefix() throws Exception {
        mockStsResponse();
        ArgumentCaptor<AssumeRoleRequest> captor = ArgumentCaptor.forClass(AssumeRoleRequest.class);

        OssStsVO vo = service.issueSts(COMPANY_ID, "job");

        assertThat(vo.getBucket()).isEqualTo("linggong-public");
        assertThat(vo.getPrefix()).isEqualTo("job/42/");

        verify(stsClient).getAcsResponse(captor.capture());
        assertPolicy(captor.getValue().getPolicy(), "linggong-public", "job/42/");
    }

    private void assertPolicy(String policy, String bucket, String prefix) throws Exception {
        JsonNode root = objectMapper.readTree(policy);
        assertThat(root.at("/Version").asText()).isEqualTo("1");
        JsonNode statement = root.at("/Statement/0");
        assertThat(statement.at("/Effect").asText()).isEqualTo("Allow");
        JsonNode action = statement.at("/Action");
        assertThat(action.isArray()).isTrue();
        assertThat(action.toString()).contains("oss:PutObject");
        assertThat(action.toString()).contains("oss:InitiateMultipartUpload");
        assertThat(action.toString()).contains("oss:UploadPart");
        assertThat(action.toString()).contains("oss:CompleteMultipartUpload");
        assertThat(action.toString()).contains("oss:AbortMultipartUpload");
        JsonNode resource = statement.at("/Resource/0");
        assertThat(resource.asText()).isEqualTo("acs:oss:*:*:" + bucket + "/" + prefix + "*");
    }

    @Test
    void issueSts_unknownBiz_shouldThrowIllegalArgument() {
        assertThatThrownBy(() -> service.issueSts(COMPANY_ID, "realname"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("不支持的业务类型");
    }

    @Test
    void issueSts_noStsClient_shouldThrowIllegalState() {
        when(stsClientProvider.getIfAvailable()).thenReturn(null);

        assertThatThrownBy(() -> service.issueSts(COMPANY_ID, "license"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("OSS未配置");
    }

    @Test
    void issueSts_blankBucket_shouldThrowIllegalState() {
        properties.setPrivateBucket("");
        assertThatThrownBy(() -> service.issueSts(COMPANY_ID, "license"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("OSS bucket 未配置");
    }

    @Test
    void issueSts_nullCompanyId_shouldThrowIllegalArgument() {
        assertThatThrownBy(() -> service.issueSts(null, "license"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
