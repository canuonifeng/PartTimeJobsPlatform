package com.parttime.enterprise.service;

import com.parttime.enterprise.mapper.OperationMapper;
import com.parttime.enterprise.pojo.vo.OperationDashboardVO;
import com.parttime.enterprise.pojo.vo.OperationTodoItemVO;
import com.parttime.enterprise.pojo.vo.PageVO;
import com.parttime.enterprise.service.impl.OperationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OperationServiceTest {

    @Mock
    private OperationMapper operationMapper;
    @Mock
    private ApplicationService applicationService;
    @Mock
    private ScheduleService scheduleService;
    @Mock
    private SettlementService settlementService;

    @InjectMocks
    private OperationServiceImpl operationService;

    @Test
    void dashboard_shouldUseScheduleConfirmCountForAttendanceTodos() {
        when(operationMapper.countApplicationTodos(1L)).thenReturn(2L);
        when(operationMapper.countScheduleTodos(eq(1L), any(LocalDate.class))).thenReturn(3L);
        when(operationMapper.countSalaryTodos(1L)).thenReturn(4L);

        OperationDashboardVO dashboard = operationService.getDashboard(1L);

        assertThat(dashboard.getOverview().getPendingTodoCount()).isEqualTo(9L);
        assertThat(dashboard.getTodoSummary())
                .extracting("type", "count")
                .contains(tuple("ATTENDANCE", 3L), tuple("SALARY", 4L));
        verify(operationMapper, never()).countAttendanceTodos(1L);
    }

    @Test
    void getTodos_shouldReturnAttendanceTypeForScheduleConfirmTodos() {
        OperationTodoItemVO item = new OperationTodoItemVO();
        item.setId("ATTENDANCE-8");
        item.setType("ATTENDANCE");
        when(operationMapper.findAttendanceConfirmTodos(eq(1L), any(LocalDate.class), eq(0), eq(20))).thenReturn(List.of(item));
        when(operationMapper.countScheduleTodos(eq(1L), any(LocalDate.class))).thenReturn(1L);

        PageVO<OperationTodoItemVO> result = operationService.getTodos(1L, "ATTENDANCE", 1, 20);

        assertThat(result.getTotal()).isEqualTo(1L);
        assertThat(result.getRecords()).singleElement().satisfies(todo -> {
            assertThat(todo.getType()).isEqualTo("ATTENDANCE");
            assertThat(todo.getActions()).isEmpty();
        });
        verify(operationMapper, never()).findAttendanceTodos(eq(1L), eq(0), eq(20));
    }
}
