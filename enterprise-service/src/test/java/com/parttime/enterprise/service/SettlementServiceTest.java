package com.parttime.enterprise.service;

import com.parttime.enterprise.mapper.AttendanceRecordMapper;
import com.parttime.enterprise.mapper.BalanceTransactionMapper;
import com.parttime.enterprise.mapper.ScheduleShiftMapper;
import com.parttime.enterprise.mapper.WorkerBalanceMapper;
import com.parttime.enterprise.mapper.WorkerNotificationMapper;
import com.parttime.enterprise.mapper.WorkerSyncMapper;
import com.parttime.enterprise.pojo.entity.AttendanceRecord;
import com.parttime.enterprise.pojo.entity.BalanceTransaction;
import com.parttime.enterprise.pojo.entity.ScheduleShift;
import com.parttime.enterprise.pojo.entity.WorkerBalance;
import com.parttime.enterprise.service.impl.SettlementServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SettlementServiceTest {

    @Mock
    private AttendanceRecordMapper attendanceRecordMapper;
    @Mock
    private ScheduleShiftMapper scheduleShiftMapper;
    @Mock
    private WorkerSyncMapper workerSyncMapper;

    @Mock
    private WorkerBalanceMapper workerBalanceMapper;
    @Mock
    private BalanceTransactionMapper balanceTransactionMapper;
    @Mock
    private EnterpriseBalanceService enterpriseBalanceService;
    @Mock
    private WorkerNotificationMapper workerNotificationMapper;

    @InjectMocks
    private SettlementServiceImpl settlementService;

    @Test
    void payFromAttendanceRecords_shouldNotifyWorkerWhenEarningsSettled() {
        AttendanceRecord record = new AttendanceRecord();
        record.setId(1L);
        record.setShiftId(2L);
        record.setWorkerId(3L);
        record.setSettlementStatus("UNPAID");
        record.setPayablePay(new BigDecimal("120.00"));

        ScheduleShift shift = new ScheduleShift();
        shift.setId(2L);
        shift.setWorkerId(3L);
        shift.setShiftDate(LocalDate.of(2026, 5, 30));

        WorkerBalance balance = new WorkerBalance();
        balance.setBalance(new BigDecimal("80.00"));
        balance.setTotalEarned(new BigDecimal("380.00"));
        balance.setTotalWithdrawn(new BigDecimal("100.00"));

        when(attendanceRecordMapper.findByIds(List.of(1L))).thenReturn(List.of(record));
        when(scheduleShiftMapper.findByIds(List.of(2L))).thenReturn(List.of(shift));
        when(workerSyncMapper.findWorkerNamesByIds(List.of(3L))).thenReturn(List.of(Map.of("id", 3L, "name", "张三")));
        when(workerBalanceMapper.findByWorkerIds(List.of(3L))).thenReturn(List.of(balance));

        settlementService.payFromAttendanceRecords(List.of(1L), 9L);

        verify(workerNotificationMapper).insertWorkerNotification(
                3L,
                "EARNINGS_SETTLED",
                "finance",
                "收入到账",
                "您有一笔兼职收入120.00元已到账",
                "ATTENDANCE",
                1L);
    }

    @Test
    void payFromAttendanceRecords_shouldRejectPaidRecords() {
        AttendanceRecord record = new AttendanceRecord();
        record.setId(1L);
        record.setSettlementStatus("PAID");
        when(attendanceRecordMapper.findByIds(List.of(1L))).thenReturn(List.of(record));

        assertThatThrownBy(() -> settlementService.payFromAttendanceRecords(List.of(1L), 9L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("已结算记录不能结算或删除");
        verify(workerBalanceMapper, never()).upsert(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void unsettle_shouldUseAttendanceLinkedEarningsTransaction() {
        AttendanceRecord record = new AttendanceRecord();
        record.setId(1L);
        record.setShiftId(2L);
        record.setWorkerId(3L);
        record.setSettlementStatus("PAID");

        ScheduleShift shift = new ScheduleShift();
        shift.setId(2L);
        shift.setShiftDate(LocalDate.of(2026, 5, 30));

        BalanceTransaction earnings = new BalanceTransaction();
        earnings.setId(10L);
        earnings.setWorkerId(3L);
        earnings.setAmount(new BigDecimal("120.00"));
        earnings.setType("EARNINGS");
        earnings.setRelatedAttendanceRecordId(1L);

        WorkerBalance balance = new WorkerBalance();
        balance.setBalance(new BigDecimal("200.00"));
        balance.setTotalEarned(new BigDecimal("500.00"));
        balance.setTotalWithdrawn(new BigDecimal("100.00"));

        when(attendanceRecordMapper.findById(1L)).thenReturn(Optional.of(record));
        when(scheduleShiftMapper.findById(2L)).thenReturn(Optional.of(shift));
        when(workerSyncMapper.findWorkerNameById(3L)).thenReturn("张三");
        when(balanceTransactionMapper.findByAttendanceRecordIdAndType(1L, "EARNINGS")).thenReturn(Optional.of(earnings));
        when(workerBalanceMapper.findByWorkerId(3L)).thenReturn(balance);

        settlementService.unsettle(1L, 9L);

        verify(workerBalanceMapper).upsert(3L, new BigDecimal("80.00"), new BigDecimal("380.00"), new BigDecimal("100.00"));
        verify(balanceTransactionMapper).insert(argThat(t ->
                "REFUND".equals(t.getType())
                        && t.getAmount().compareTo(new BigDecimal("-120.00")) == 0
                        && Long.valueOf(1L).equals(t.getRelatedAttendanceRecordId())));
        verify(enterpriseBalanceService).refund(9L, new BigDecimal("120.00"), null, "撤回结算退款: 张三 2026-05-30");
    }
}
