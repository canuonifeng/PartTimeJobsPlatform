package com.parttime.cservice.service;

import com.parttime.cservice.config.JwtTokenProvider;
import com.parttime.cservice.service.impl.WorkerServiceImpl;
import com.parttime.cservice.pojo.vo.LoginVO;
import com.parttime.cservice.pojo.cmd.RegisterCmd;
import com.parttime.cservice.pojo.vo.WorkerVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class WorkerServiceTest {

    @InjectMocks
    private WorkerServiceImpl workerService;

    private static final String SECRET = "parttime-cservice-jwt-secret-key-must-be-at-least-256-bits";
    private static final long EXPIRATION = 86400000L;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(workerService, "workerMapper", InMemoryMappers.createWorkerMapper());
        ReflectionTestUtils.setField(workerService, "workerProfileMapper", InMemoryMappers.createWorkerProfileMapper());
        ReflectionTestUtils.setField(workerService, "jwtTokenProvider", new JwtTokenProvider(SECRET, EXPIRATION));
    }

    @Test
    void register_shouldCreateWorkerAndReturnResponse() {
        RegisterCmd request = new RegisterCmd("John", "13800138000", "http://avatar.url");
        WorkerVO response = workerService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getName()).isEqualTo("John");
        assertThat(response.getPhone()).isEqualTo("13800138000");
        assertThat(response.getAvatar()).isEqualTo("http://avatar.url");
        assertThat(response.getCreatedAt()).isNotNull();
    }

    @Test
    void register_shouldGenerateSequentialIds() {
        WorkerVO r1 = workerService.register(new RegisterCmd("A", null, null));
        WorkerVO r2 = workerService.register(new RegisterCmd("B", null, null));

        assertThat(r2.getId()).isGreaterThan(r1.getId());
    }

    @Test
    void getWorkerById_shouldReturnRegisteredWorker() {
        WorkerVO created = workerService.register(new RegisterCmd("John", "13800138000", null));
        WorkerVO found = workerService.getWorkerById(created.getId());

        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getName()).isEqualTo("John");
    }

    @Test
    void login_withWechatCode_shouldReturnToken() {
        String token = workerService.login("wx_test_code_123");

        assertThat(token).isNotNull();
        assertThat(token).isNotBlank();
    }

    @Test
    void login_withSameCode_shouldReturnSameWorker() {
        String token1 = workerService.login("wx_same_code");
        String token2 = workerService.login("wx_same_code");

        assertThat(token1).isNotNull();
        assertThat(token2).isNotNull();
    }

    @Test
    void login_shouldCreateWorkerThatCanBeRetrieved() {
        String token = workerService.login("wx_new_worker");
        JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(SECRET, EXPIRATION);
        String workerId = jwtTokenProvider.getUserIdFromToken(token);

        WorkerVO worker = workerService.getWorkerById(Long.valueOf(workerId));
        assertThat(worker).isNotNull();
        assertThat(worker.getId()).isEqualTo(Long.valueOf(workerId));
    }

    @Test
    void updateProfile_shouldModifyFields() {
        WorkerVO created = workerService.register(new RegisterCmd("John", null, null));

        RegisterCmd update = new RegisterCmd("John Updated", "13900139000", "http://new.avatar");
        WorkerVO updated = workerService.updateProfile(created.getId(), update);

        assertThat(updated.getName()).isEqualTo("John Updated");
        assertThat(updated.getPhone()).isEqualTo("13900139000");
        assertThat(updated.getAvatar()).isEqualTo("http://new.avatar");
    }

    @Test
    void updateProfile_shouldKeepExistingFieldsWhenNull() {
        WorkerVO created = workerService.register(new RegisterCmd("John", "13800138000", "http://avatar"));

        WorkerVO updated = workerService.updateProfile(created.getId(), new RegisterCmd("New Name", null, null));

        assertThat(updated.getName()).isEqualTo("New Name");
        assertThat(updated.getPhone()).isEqualTo("13800138000");
        assertThat(updated.getAvatar()).isEqualTo("http://avatar");
    }

    @Test
    void loginWithWechat_shouldReturnResponseWithToken() {
        LoginVO response = workerService.loginWithWechat("test_code");

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isNotBlank();
        assertThat(response.getWorkerId()).isNotNull();
        assertThat(response.getOpenId()).isEqualTo("openid_test_code");
    }

    @Test
    void loginWithWechat_sameCode_shouldReturnSameWorker() {
        LoginVO r1 = workerService.loginWithWechat("same_code");
        LoginVO r2 = workerService.loginWithWechat("same_code");

        assertThat(r1.getWorkerId()).isEqualTo(r2.getWorkerId());
        assertThat(r1.getOpenId()).isEqualTo(r2.getOpenId());
    }

    @Test
    void loginWithWechat_differentCodes_shouldReturnDifferentWorkers() {
        LoginVO r1 = workerService.loginWithWechat("code_1");
        LoginVO r2 = workerService.loginWithWechat("code_2");

        assertThat(r1.getWorkerId()).isNotEqualTo(r2.getWorkerId());
        assertThat(r1.getOpenId()).isNotEqualTo(r2.getOpenId());
    }

    @Test
    void getWorkerByOpenId_shouldReturnWorker() {
        LoginVO loginResponse = workerService.loginWithWechat("find_by_openid");
        WorkerVO worker = workerService.getWorkerByOpenId("openid_find_by_openid");

        assertThat(worker).isNotNull();
        assertThat(worker.getId()).isEqualTo(loginResponse.getWorkerId());
    }
}
