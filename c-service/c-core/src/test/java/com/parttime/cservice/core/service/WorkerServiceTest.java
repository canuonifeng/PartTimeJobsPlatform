package com.parttime.cservice.core.service;

import com.parttime.cservice.core.auth.JwtTokenProvider;
import com.parttime.cservice.core.dto.WorkerLoginRequest;
import com.parttime.cservice.core.dto.WorkerRegisterRequest;
import com.parttime.cservice.core.dto.WorkerResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WorkerServiceTest {

    private WorkerService workerService;

    private static final String SECRET = "parttime-cservice-jwt-secret-key-must-be-at-least-256-bits";
    private static final long EXPIRATION = 86400000L;

    @BeforeEach
    void setUp() {
        JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(SECRET, EXPIRATION);
        workerService = new WorkerService(jwtTokenProvider);
    }

    @Test
    void register_shouldCreateWorkerAndReturnResponse() {
        WorkerRegisterRequest request = new WorkerRegisterRequest("John", "13800138000", "http://avatar.url");
        WorkerResponse response = workerService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getName()).isEqualTo("John");
        assertThat(response.getPhone()).isEqualTo("13800138000");
        assertThat(response.getAvatar()).isEqualTo("http://avatar.url");
        assertThat(response.getCreatedAt()).isNotNull();
    }

    @Test
    void register_shouldGenerateSequentialIds() {
        WorkerResponse r1 = workerService.register(new WorkerRegisterRequest("A", null, null));
        WorkerResponse r2 = workerService.register(new WorkerRegisterRequest("B", null, null));

        assertThat(r2.getId()).isGreaterThan(r1.getId());
    }

    @Test
    void getWorkerById_shouldReturnRegisteredWorker() {
        WorkerResponse created = workerService.register(new WorkerRegisterRequest("John", "13800138000", null));
        WorkerResponse found = workerService.getWorkerById(created.getId());

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

        WorkerResponse worker = workerService.getWorkerById(Long.valueOf(workerId));
        assertThat(worker).isNotNull();
        assertThat(worker.getId()).isEqualTo(Long.valueOf(workerId));
    }

    @Test
    void updateProfile_shouldModifyFields() {
        WorkerResponse created = workerService.register(new WorkerRegisterRequest("John", null, null));

        WorkerRegisterRequest update = new WorkerRegisterRequest("John Updated", "13900139000", "http://new.avatar");
        WorkerResponse updated = workerService.updateProfile(created.getId(), update);

        assertThat(updated.getName()).isEqualTo("John Updated");
        assertThat(updated.getPhone()).isEqualTo("13900139000");
        assertThat(updated.getAvatar()).isEqualTo("http://new.avatar");
    }

    @Test
    void updateProfile_shouldKeepExistingFieldsWhenNull() {
        WorkerResponse created = workerService.register(new WorkerRegisterRequest("John", "13800138000", "http://avatar"));

        WorkerResponse updated = workerService.updateProfile(created.getId(), new WorkerRegisterRequest("New Name", null, null));

        assertThat(updated.getName()).isEqualTo("New Name");
        assertThat(updated.getPhone()).isEqualTo("13800138000");
        assertThat(updated.getAvatar()).isEqualTo("http://avatar");
    }
}
