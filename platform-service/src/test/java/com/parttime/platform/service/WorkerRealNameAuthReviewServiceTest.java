package com.parttime.platform.service;

import com.parttime.platform.mapper.WorkerNotificationMapper;
import com.parttime.platform.mapper.WorkerRealNameAuthMapper;
import com.parttime.platform.pojo.entity.WorkerRealNameAuth;
import com.parttime.platform.service.impl.WorkerRealNameAuthReviewServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkerRealNameAuthReviewServiceTest {

    @Mock
    private WorkerRealNameAuthMapper mapper;

    @Mock
    private WorkerNotificationMapper workerNotificationMapper;

    @InjectMocks
    private WorkerRealNameAuthReviewServiceImpl service;

    @Test
    void approve_shouldCreateSystemNotificationForWorker() {
        WorkerRealNameAuth auth = pendingAuth();
        when(mapper.findById(1L)).thenReturn(Optional.of(auth));

        service.approve(1L, 99L);

        verify(workerNotificationMapper).insertWorkerNotification(
                10L,
                "REAL_NAME_APPROVED",
                "system",
                "实名认证已通过",
                "您的实名认证已通过审核",
                "REAL_NAME_AUTH",
                1L);
    }

    @Test
    void reject_shouldCreateSystemNotificationForWorker() {
        WorkerRealNameAuth auth = pendingAuth();
        when(mapper.findById(1L)).thenReturn(Optional.of(auth));

        service.reject(1L, 99L, "证件照片不清晰");

        verify(workerNotificationMapper).insertWorkerNotification(
                10L,
                "REAL_NAME_REJECTED",
                "system",
                "实名认证未通过",
                "您的实名认证未通过审核，原因：证件照片不清晰",
                "REAL_NAME_AUTH",
                1L);
    }

    private WorkerRealNameAuth pendingAuth() {
        WorkerRealNameAuth auth = new WorkerRealNameAuth();
        auth.setId(1L);
        auth.setWorkerId(10L);
        auth.setStatus("PENDING");
        return auth;
    }
}
