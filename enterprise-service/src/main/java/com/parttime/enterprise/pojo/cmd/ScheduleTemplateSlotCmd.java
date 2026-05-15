package com.parttime.enterprise.pojo.cmd;

import java.math.BigDecimal;
import java.time.LocalTime;

public class ScheduleTemplateSlotCmd {

    private Integer dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer maxWorkers;
    private BigDecimal locationLat;
    private BigDecimal locationLng;
    private Integer locationRadius;
    private String locationName;

    public Integer getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(Integer dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public Integer getMaxWorkers() { return maxWorkers; }
    public void setMaxWorkers(Integer maxWorkers) { this.maxWorkers = maxWorkers; }

    public BigDecimal getLocationLat() { return locationLat; }
    public void setLocationLat(BigDecimal locationLat) { this.locationLat = locationLat; }

    public BigDecimal getLocationLng() { return locationLng; }
    public void setLocationLng(BigDecimal locationLng) { this.locationLng = locationLng; }

    public Integer getLocationRadius() { return locationRadius; }
    public void setLocationRadius(Integer locationRadius) { this.locationRadius = locationRadius; }

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }
}
