package com.parttime.cservice.pojo.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AttendanceRecordEntity {

    private Long id;
    private Long shiftId;
    private Long workerId;
    private LocalDateTime checkInTime;
    private BigDecimal checkInLat;
    private BigDecimal checkInLng;
    private LocalDateTime checkOutTime;
    private BigDecimal checkOutLat;
    private BigDecimal checkOutLng;
    private BigDecimal totalHours;
    private String status;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AttendanceRecordEntity() {}

    public AttendanceRecordEntity(Long id, Long shiftId, Long workerId, LocalDateTime checkInTime,
                             BigDecimal checkInLat, BigDecimal checkInLng, LocalDateTime checkOutTime,
                             BigDecimal checkOutLat, BigDecimal checkOutLng,
                             BigDecimal totalHours, String status) {
        this.id = id;
        this.shiftId = shiftId;
        this.workerId = workerId;
        this.checkInTime = checkInTime;
        this.checkInLat = checkInLat;
        this.checkInLng = checkInLng;
        this.checkOutTime = checkOutTime;
        this.checkOutLat = checkOutLat;
        this.checkOutLng = checkOutLng;
        this.totalHours = totalHours;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getShiftId() { return shiftId; }
    public void setShiftId(Long shiftId) { this.shiftId = shiftId; }

    public Long getWorkerId() { return workerId; }
    public void setWorkerId(Long workerId) { this.workerId = workerId; }

    public LocalDateTime getCheckInTime() { return checkInTime; }
    public void setCheckInTime(LocalDateTime checkInTime) { this.checkInTime = checkInTime; }

    public BigDecimal getCheckInLat() { return checkInLat; }
    public void setCheckInLat(BigDecimal checkInLat) { this.checkInLat = checkInLat; }

    public BigDecimal getCheckInLng() { return checkInLng; }
    public void setCheckInLng(BigDecimal checkInLng) { this.checkInLng = checkInLng; }

    public LocalDateTime getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(LocalDateTime checkOutTime) { this.checkOutTime = checkOutTime; }

    public BigDecimal getCheckOutLat() { return checkOutLat; }
    public void setCheckOutLat(BigDecimal checkOutLat) { this.checkOutLat = checkOutLat; }

    public BigDecimal getCheckOutLng() { return checkOutLng; }
    public void setCheckOutLng(BigDecimal checkOutLng) { this.checkOutLng = checkOutLng; }

    public BigDecimal getTotalHours() { return totalHours; }
    public void setTotalHours(BigDecimal totalHours) { this.totalHours = totalHours; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
