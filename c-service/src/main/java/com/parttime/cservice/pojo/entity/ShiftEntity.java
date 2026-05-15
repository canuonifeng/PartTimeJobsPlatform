package com.parttime.cservice.pojo.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class ShiftEntity {

    private Long id;
    private Long jobId;
    private String jobTitle;
    private String jobLocation;
    private Long workerId;
    private LocalDate shiftDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal locationLat;
    private BigDecimal locationLng;
    private Integer locationRadius;
    private String locationName;
    private String status;

    public ShiftEntity() {}

    public ShiftEntity(Long id, Long jobId, String jobTitle, String jobLocation, Long workerId,
                        LocalDate shiftDate, LocalTime startTime, LocalTime endTime,
                        BigDecimal locationLat, BigDecimal locationLng, Integer locationRadius,
                        String locationName, String status) {
        this.id = id;
        this.jobId = jobId;
        this.jobTitle = jobTitle;
        this.jobLocation = jobLocation;
        this.workerId = workerId;
        this.shiftDate = shiftDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.locationLat = locationLat;
        this.locationLng = locationLng;
        this.locationRadius = locationRadius;
        this.locationName = locationName;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getJobId() { return jobId; }
    public void setJobId(Long jobId) { this.jobId = jobId; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getJobLocation() { return jobLocation; }
    public void setJobLocation(String jobLocation) { this.jobLocation = jobLocation; }

    public Long getWorkerId() { return workerId; }
    public void setWorkerId(Long workerId) { this.workerId = workerId; }

    public LocalDate getShiftDate() { return shiftDate; }
    public void setShiftDate(LocalDate shiftDate) { this.shiftDate = shiftDate; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public BigDecimal getLocationLat() { return locationLat; }
    public void setLocationLat(BigDecimal locationLat) { this.locationLat = locationLat; }

    public BigDecimal getLocationLng() { return locationLng; }
    public void setLocationLng(BigDecimal locationLng) { this.locationLng = locationLng; }

    public Integer getLocationRadius() { return locationRadius; }
    public void setLocationRadius(Integer locationRadius) { this.locationRadius = locationRadius; }

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
