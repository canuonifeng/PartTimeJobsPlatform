package com.parttime.enterprise.service;

import com.parttime.enterprise.mapper.AttendanceRecordMapper;
import com.parttime.enterprise.pojo.entity.AttendanceRecord;
import com.parttime.enterprise.service.impl.AttendanceHoursServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttendanceHoursServiceTest {

    @Mock
    private AttendanceRecordMapper attendanceRecordMapper;

    @InjectMocks
    private AttendanceHoursServiceImpl attendanceHoursService;

    @Test
    void batchDelete_shouldRejectPaidRecords() {
        AttendanceRecord record = new AttendanceRecord();
        record.setId(1L);
        record.setSettlementStatus("PAID");
        when(attendanceRecordMapper.findByIds(List.of(1L))).thenReturn(List.of(record));

        assertThatThrownBy(() -> attendanceHoursService.batchDelete(List.of(1L)))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("已结算记录不能结算或删除");
        verify(attendanceRecordMapper, never()).deleteByIds(List.of(1L));
    }
}
