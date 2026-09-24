package com.parttime.platform.service;

import com.aliyuncs.IAcsClient;
import com.aliyuncs.sts.model.v20150401.AssumeRoleRequest;
import com.aliyuncs.sts.model.v20150401.AssumeRoleResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.parttime.platform.config.OssProperties;
import com.parttime.platform.pojo.vo.OssStsVO;
import com.parttime.platform.service.impl.OssStsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OssStsServiceImplTest {

    private static final String ADMIN_NAME = "admin";
    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

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
        properties.setStsRoleSessionName("linggong-admin");

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
    void issueSts_training_shouldUsePublicBucketAndDailyPrefix() throws Exception {
        mockStsResponse();
        ArgumentCaptor<AssumeRoleRequest> captor = ArgumentCaptor.forClass(AssumeRoleRequest.class);

        OssStsVO vo = service.issueSts(ADMIN_NAME, "training");

        String day = LocalDate.now().format(DAY_FORMATTER);
        assertThat(vo.getBucket()).isEqualTo("linggong-public");
        assertThat(vo.getPrefix()).isEqualTo("training/" + day + "/");
        assertThat(vo.getEndpoint()).isEqualTo("oss-cn-hangzhou.aliyuncs.com");
        assertThat(vo.getAccessKeyId()).isEqualTo("LTAI-test");
        assertThat(vo.getAccessKeySecret()).isEqualTo("secret-test");
        assertThat(vo.getSecurityToken()).isEqualTo("token-test");
        assertThat(vo.getExpiration()).isEqualTo("2026-09-24T12:00:00Z");

        verify(stsClient).getAcsResponse(captor.capture());
        AssumeRoleRequest req = captor.getValue();
        assertThat(req.getRoleArn()).isEqualTo("acs:ram::1234:role/linggong-oss");
        assertThat(req.getRoleSessionName()).isEqualTo("linggong-admin-admin");
        assertPolicy(req.getPolicy(), "linggong-public", "training/" + day + "/");
    }

    @Test
    void issueSts_material_shouldUsePublicBucketAndDailyPrefix() throws Exception {
        mockStsResponse();
        ArgumentCaptor<AssumeRoleRequest> captor = ArgumentCaptor.forClass(AssumeRoleRequest.class);

        OssStsVO vo = service.issueSts(ADMIN_NAME, "material");

        String day = LocalDate.now().format(DAY_FORMATTER);
        assertThat(vo.getBucket()).isEqualTo("linggong-public");
        assertThat(vo.getPrefix()).isEqualTo("material/" + day + "/");

        verify(stsClient).getAcsResponse(captor.capture());
        assertPolicy(captor.getValue().getPolicy(), "linggong-public", "material/" + day + "/");
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
        assertThatThrownBy(() -> service.issueSts(ADMIN_NAME, "realname"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("不支持的业务类型");
        assertThatThrownBy(() -> service.issueSts(ADMIN_NAME, "job"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.issueSts(ADMIN_NAME, "avatar"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void issueSts_noStsClient_shouldThrowIllegalState() {
        when(stsClientProvider.getIfAvailable()).thenReturn(null);

        assertThatThrownBy(() -> service.issueSts(ADMIN_NAME, "training"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("OSS未配置");
    }

    @Test
    void issueSts_blankBucket_shouldThrowIllegalState() {
        properties.setPublicBucket("");
        assertThatThrownBy(() -> service.issueSts(ADMIN_NAME, "training"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("OSS bucket 未配置");
    }
}
