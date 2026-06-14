package com.parttime.enterprise.mapper;

import com.parttime.enterprise.pojo.vo.OperationExceptionVO;
import com.parttime.enterprise.pojo.vo.OperationTodoItemVO;
import com.parttime.enterprise.pojo.vo.OperationTrendPointVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface OperationMapper {
    long countPublishedJobs(@Param("companyId") Long companyId);
    long countApplications(@Param("companyId") Long companyId);
    long countPendingApplications(@Param("companyId") Long companyId);
    long countTodayApplications(@Param("companyId") Long companyId, @Param("today") LocalDate today);
    long countTodayShifts(@Param("companyId") Long companyId, @Param("today") LocalDate today);
    long countFutureShifts(@Param("companyId") Long companyId, @Param("today") LocalDate today);
    long countAttendanceExceptions(@Param("companyId") Long companyId, @Param("today") LocalDate today);
    long countUnpaidSalary(@Param("companyId") Long companyId);
    List<OperationTodoItemVO> findApplicationTodos(@Param("companyId") Long companyId, @Param("offset") int offset, @Param("pageSize") int pageSize);
    long countApplicationTodos(@Param("companyId") Long companyId);
    List<OperationTodoItemVO> findScheduleTodos(@Param("companyId") Long companyId, @Param("today") LocalDate today, @Param("offset") int offset, @Param("pageSize") int pageSize);
    long countScheduleTodos(@Param("companyId") Long companyId, @Param("today") LocalDate today);
    List<OperationTodoItemVO> findAttendanceConfirmTodos(@Param("companyId") Long companyId, @Param("today") LocalDate today, @Param("offset") int offset, @Param("pageSize") int pageSize);
    List<OperationTodoItemVO> findAttendanceTodos(@Param("companyId") Long companyId, @Param("offset") int offset, @Param("pageSize") int pageSize);
    long countAttendanceTodos(@Param("companyId") Long companyId);
    List<OperationTodoItemVO> findSalaryTodos(@Param("companyId") Long companyId, @Param("offset") int offset, @Param("pageSize") int pageSize);
    long countSalaryTodos(@Param("companyId") Long companyId);
    List<OperationExceptionVO> findExceptions(@Param("companyId") Long companyId, @Param("offset") int offset, @Param("pageSize") int pageSize);
    long countExceptions(@Param("companyId") Long companyId);
    List<OperationTrendPointVO> findTrendPoints(@Param("companyId") Long companyId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
