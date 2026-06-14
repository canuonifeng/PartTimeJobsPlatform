package com.parttime.enterprise.service;

import com.parttime.enterprise.mapper.CompanyWorkerMapper;
import com.parttime.enterprise.mapper.WorkerSyncMapper;
import com.parttime.enterprise.pojo.entity.CompanyWorker;
import com.parttime.enterprise.pojo.vo.WorkerListVO;
import com.parttime.enterprise.service.impl.CompanyWorkerServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyWorkerServiceTest {

    @Mock
    private CompanyWorkerMapper companyWorkerMapper;
    @Mock
    private WorkerSyncMapper workerSyncMapper;

    @InjectMocks
    private CompanyWorkerServiceImpl companyWorkerService;

    @Test
    void list_shouldBatchLoadWorkerProfiles() {
        CompanyWorker worker = new CompanyWorker();
        worker.setId(1L);
        worker.setWorkerId(10L);
        worker.setStatus("ACTIVE");

        when(companyWorkerMapper.findByCompanyId(2L, "张")).thenReturn(List.of(worker));
        when(workerSyncMapper.findWorkerNamesByIds(List.of(10L))).thenReturn(List.of(Map.of("id", 10L, "name", "张三")));
        when(workerSyncMapper.findWorkerPhonesByIds(List.of(10L))).thenReturn(List.of(Map.of("id", 10L, "phone", "13800000000")));
        when(workerSyncMapper.findWorkerGendersByIds(List.of(10L))).thenReturn(List.of(Map.of("worker_id", 10L, "gender", "MALE")));
        when(workerSyncMapper.findWorkerBirthdaysByIds(List.of(10L))).thenReturn(List.of(Map.of("worker_id", 10L, "birthday", Date.valueOf(LocalDate.now().minusYears(25)))));
        when(workerSyncMapper.findWorkerRealNameStatusesByIds(List.of(10L))).thenReturn(List.of(Map.of("worker_id", 10L, "status", "APPROVED")));

        List<WorkerListVO> result = companyWorkerService.list(2L, "张");

        assertThat(result).singleElement().satisfies(vo -> {
            assertThat(vo.getName()).isEqualTo("张三");
            assertThat(vo.getPhone()).isEqualTo("13800000000");
            assertThat(vo.getWorkerGender()).isEqualTo("MALE");
            assertThat(vo.getWorkerAge()).isEqualTo(25);
            assertThat(vo.getRealNameStatus()).isEqualTo("APPROVED");
        });
        verify(workerSyncMapper, never()).findWorkerNameById(10L);
        verify(workerSyncMapper, never()).findWorkerPhoneById(10L);
        verify(workerSyncMapper, never()).findWorkerGenderById(10L);
        verify(workerSyncMapper, never()).findWorkerBirthdayById(10L);
        verify(workerSyncMapper, never()).findWorkerRealNameStatusById(10L);
    }
}
