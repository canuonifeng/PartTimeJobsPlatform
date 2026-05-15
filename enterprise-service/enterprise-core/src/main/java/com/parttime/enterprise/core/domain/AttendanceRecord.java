package com.parttime.enterprise.core.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AttendanceRecord {

    private Long id;
    private Long shiftId;
    private LocalDateTime checkInTime;
    private BigDecimal checkInLat;
    private BigDecimal checkInLng;
    private LocalDateTime checkOutTime;
    private BigDecimal checkOutLat;
    private BigDecimal checkOutLng;
    private BigDecimal totalHours;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getShiftId() { return shiftId; }
    public void setShiftId(Long shiftId) { this.shiftId = shiftId; }

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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
