package com.parttime.enterprise.core.service;

import com.parttime.enterprise.api.dto.PayrollBatchRequest;
import com.parttime.enterprise.api.dto.PayrollBatchResponse;
import com.parttime.enterprise.api.dto.PayrollItemResponse;
import com.parttime.enterprise.core.domain.AttendanceRecord;
import com.parttime.enterprise.core.domain.JobRate;
import com.parttime.enterprise.core.domain.PayrollBatch;
import com.parttime.enterprise.core.domain.PayrollItem;
import com.parttime.enterprise.core.domain.ScheduleShift;
import com.parttime.enterprise.core.repository.AttendanceRecordRepository;
import com.parttime.enterprise.core.repository.JobRepository;
import com.parttime.enterprise.core.repository.PayrollBatchRepository;
import com.parttime.enterprise.core.repository.PayrollItemRepository;
import com.parttime.enterprise.core.repository.ScheduleShiftRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayrollServiceTest {

    @Mock
    private PayrollBatchRepository payrollBatchRepository;

    @Mock
    private PayrollItemRepository payrollItemRepository;

    @Mock
    private ScheduleShiftRepository scheduleShiftRepository;

    @Mock
    private AttendanceRecordRepository attendanceRecordRepository;

    @Mock
    private JobRepository jobRepository;

    @Captor
    private ArgumentCaptor<PayrollBatch> batchCaptor;

    @Captor
    private ArgumentCaptor<List<PayrollItem>> itemsCaptor;

    private PayrollService payrollService;

    @BeforeEach
    void setUp() {
        payrollService = new PayrollService(payrollBatchRepository, payrollItemRepository,
                scheduleShiftRepository, attendanceRecordRepository, jobRepository);
    }

    @Test
    void createBatch_shouldCreateAndReturnResponse() {
        PayrollBatchRequest request = new PayrollBatchRequest();
        request.setCompanyId(1L);
        request.setName("June 2026 Payroll");
        request.setPeriodStart(LocalDate.of(2026, 6, 1));
        request.setPeriodEnd(LocalDate.of(2026, 6, 30));

        doAnswer(invocation -> {
            PayrollBatch batch = invocation.getArgument(0);
            batch.setId(100L);
            return null;
        }).when(payrollBatchRepository).save(any(PayrollBatch.class));

        PayrollBatchResponse response = payrollService.createBatch(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getCompanyId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("June 2026 Payroll");
        assertThat(response.getStatus()).isEqualTo("DRAFT");
        assertThat(response.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.getWorkerCount()).isZero();

        verify(payrollBatchRepository).save(batchCaptor.capture());
        assertThat(batchCaptor.getValue().getCompanyId()).isEqualTo(1L);
        assertThat(batchCaptor.getValue().getStatus()).isEqualTo("DRAFT");
    }

    @Test
    void calculateBatch_shouldComputePayForHourlyRate() {
        PayrollBatch batch = new PayrollBatch();
        batch.setId(1L);
        batch.setCompanyId(1L);
        batch.setPeriodStart(LocalDate.of(2026, 6, 1));
        batch.setPeriodEnd(LocalDate.of(2026, 6, 30));
        batch.setStatus("DRAFT");

        ScheduleShift shift1 = new ScheduleShift();
        shift1.setId(10L);
        shift1.setWorkerId(100L);
        shift1.setJobId(200L);
        shift1.setShiftDate(LocalDate.of(2026, 6, 1));

        ScheduleShift shift2 = new ScheduleShift();
        shift2.setId(11L);
        shift2.setWorkerId(100L);
        shift2.setJobId(200L);
        shift2.setShiftDate(LocalDate.of(2026, 6, 2));

        AttendanceRecord att1 = new AttendanceRecord();
        att1.setShiftId(10L);
        att1.setTotalHours(new BigDecimal("8.00"));

        AttendanceRecord att2 = new AttendanceRecord();
        att2.setShiftId(11L);
        att2.setTotalHours(new BigDecimal("7.50"));

        JobRate rate = new JobRate();
        rate.setJobId(200L);
        rate.setType("HOURLY");
        rate.setAmount(new BigDecimal("25.00"));

        when(payrollBatchRepository.findById(1L)).thenReturn(Optional.of(batch));
        when(scheduleShiftRepository.findByDateRange(batch.getPeriodStart(), batch.getPeriodEnd()))
                .thenReturn(List.of(shift1, shift2));
        when(attendanceRecordRepository.findByShiftIds(List.of(10L, 11L))).thenReturn(List.of(att1, att2));
        when(jobRepository.findRatesByJobId(200L)).thenReturn(List.of(rate));

        PayrollBatchResponse response = payrollService.calculateBatch(1L);

        assertThat(response.getStatus()).isEqualTo("CALCULATED");
        assertThat(response.getTotalAmount()).isEqualByComparingTo(new BigDecimal("387.50"));

        verify(payrollItemRepository).saveAll(itemsCaptor.capture());
        List<PayrollItem> items = itemsCaptor.getValue();
        assertThat(items).hasSize(1);
        assertThat(items.get(0).getWorkerId()).isEqualTo(100L);
        assertThat(items.get(0).getJobId()).isEqualTo(200L);
        assertThat(items.get(0).getTotalHours()).isEqualByComparingTo(new BigDecimal("15.50"));
        assertThat(items.get(0).getRateType()).isEqualTo("HOURLY");
        assertThat(items.get(0).getTotalPay()).isEqualByComparingTo(new BigDecimal("387.50"));

        verify(payrollBatchRepository).update(batchCaptor.capture());
        assertThat(batchCaptor.getValue().getStatus()).isEqualTo("CALCULATED");
    }

    @Test
    void calculateBatch_shouldComputePayForDailyRate() {
        PayrollBatch batch = new PayrollBatch();
        batch.setId(1L);
        batch.setCompanyId(1L);
        batch.setPeriodStart(LocalDate.of(2026, 6, 1));
        batch.setPeriodEnd(LocalDate.of(2026, 6, 30));
        batch.setStatus("DRAFT");

        ScheduleShift shift1 = new ScheduleShift();
        shift1.setId(10L);
        shift1.setWorkerId(100L);
        shift1.setJobId(200L);
        shift1.setShiftDate(LocalDate.of(2026, 6, 1));

        ScheduleShift shift2 = new ScheduleShift();
        shift2.setId(11L);
        shift2.setWorkerId(100L);
        shift2.setJobId(200L);
        shift2.setShiftDate(LocalDate.of(2026, 6, 2));

        AttendanceRecord att1 = new AttendanceRecord();
        att1.setShiftId(10L);
        att1.setTotalHours(new BigDecimal("8.00"));

        AttendanceRecord att2 = new AttendanceRecord();
        att2.setShiftId(11L);
        att2.setTotalHours(new BigDecimal("8.00"));

        JobRate rate = new JobRate();
        rate.setJobId(200L);
        rate.setType("DAILY");
        rate.setAmount(new BigDecimal("200.00"));

        when(payrollBatchRepository.findById(1L)).thenReturn(Optional.of(batch));
        when(scheduleShiftRepository.findByDateRange(batch.getPeriodStart(), batch.getPeriodEnd()))
                .thenReturn(List.of(shift1, shift2));
        when(attendanceRecordRepository.findByShiftIds(List.of(10L, 11L))).thenReturn(List.of(att1, att2));
        when(jobRepository.findRatesByJobId(200L)).thenReturn(List.of(rate));

        PayrollBatchResponse response = payrollService.calculateBatch(1L);

        assertThat(response.getStatus()).isEqualTo("CALCULATED");
        assertThat(response.getTotalAmount()).isEqualByComparingTo(new BigDecimal("400.00"));

        verify(payrollItemRepository).saveAll(itemsCaptor.capture());
        List<PayrollItem> items = itemsCaptor.getValue();
        assertThat(items).hasSize(1);
        assertThat(items.get(0).getTotalPay()).isEqualByComparingTo(new BigDecimal("400.00"));
    }

    @Test
    void calculateBatch_shouldThrowWhenNotDraft() {
        PayrollBatch batch = new PayrollBatch();
        batch.setId(1L);
        batch.setStatus("CALCULATED");

        when(payrollBatchRepository.findById(1L)).thenReturn(Optional.of(batch));

        assertThatThrownBy(() -> payrollService.calculateBatch(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot calculate");
    }

    @Test
    void confirmBatch_shouldTransitionToConfirmed() {
        PayrollBatch batch = new PayrollBatch();
        batch.setId(1L);
        batch.setStatus("CALCULATED");

        when(payrollBatchRepository.findById(1L)).thenReturn(Optional.of(batch));

        PayrollBatchResponse response = payrollService.confirmBatch(1L);

        assertThat(response.getStatus()).isEqualTo("CONFIRMED");
        verify(payrollBatchRepository).updateStatus(1L, "CONFIRMED");
    }

    @Test
    void confirmBatch_shouldThrowWhenNotCalculated() {
        PayrollBatch batch = new PayrollBatch();
        batch.setId(1L);
        batch.setStatus("DRAFT");

        when(payrollBatchRepository.findById(1L)).thenReturn(Optional.of(batch));

        assertThatThrownBy(() -> payrollService.confirmBatch(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot confirm");
    }

    @Test
    void payBatch_shouldTransitionToPaid() {
        PayrollBatch batch = new PayrollBatch();
        batch.setId(1L);
        batch.setStatus("CONFIRMED");

        when(payrollBatchRepository.findById(1L)).thenReturn(Optional.of(batch));

        PayrollBatchResponse response = payrollService.payBatch(1L);

        assertThat(response.getStatus()).isEqualTo("PAID");
        verify(payrollBatchRepository).updateStatus(1L, "PAID");
    }

    @Test
    void payBatch_shouldThrowWhenNotConfirmed() {
        PayrollBatch batch = new PayrollBatch();
        batch.setId(1L);
        batch.setStatus("DRAFT");

        when(payrollBatchRepository.findById(1L)).thenReturn(Optional.of(batch));

        assertThatThrownBy(() -> payrollService.payBatch(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot pay");
    }

    @Test
    void getBatchById_shouldReturnBatch() {
        PayrollBatch batch = new PayrollBatch();
        batch.setId(1L);
        batch.setCompanyId(1L);
        batch.setName("Test Batch");
        batch.setStatus("DRAFT");

        when(payrollBatchRepository.findById(1L)).thenReturn(Optional.of(batch));

        PayrollBatchResponse response = payrollService.getBatchById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Test Batch");
    }

    @Test
    void getBatchById_shouldThrowWhenNotFound() {
        when(payrollBatchRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> payrollService.getBatchById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void getBatchesByCompany_shouldReturnBatches() {
        PayrollBatch batch1 = new PayrollBatch();
        batch1.setId(1L);
        batch1.setCompanyId(1L);

        PayrollBatch batch2 = new PayrollBatch();
        batch2.setId(2L);
        batch2.setCompanyId(1L);

        when(payrollBatchRepository.findByCompanyId(1L)).thenReturn(List.of(batch1, batch2));

        List<PayrollBatchResponse> batches = payrollService.getBatchesByCompany(1L);

        assertThat(batches).hasSize(2);
    }

    @Test
    void getBatchItems_shouldReturnItems() {
        PayrollBatch batch = new PayrollBatch();
        batch.setId(1L);
        batch.setStatus("CALCULATED");

        PayrollItem item = new PayrollItem();
        item.setId(1L);
        item.setBatchId(1L);
        item.setWorkerId(100L);
        item.setTotalPay(new BigDecimal("500.00"));

        when(payrollBatchRepository.findById(1L)).thenReturn(Optional.of(batch));
        when(payrollItemRepository.findByBatchId(1L)).thenReturn(List.of(item));

        List<PayrollItemResponse> items = payrollService.getBatchItems(1L);

        assertThat(items).hasSize(1);
        assertThat(items.get(0).getWorkerId()).isEqualTo(100L);
        assertThat(items.get(0).getTotalPay()).isEqualByComparingTo(new BigDecimal("500.00"));
    }

    @Test
    void getBatchItems_shouldThrowWhenBatchNotFound() {
        when(payrollBatchRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> payrollService.getBatchItems(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }
}
