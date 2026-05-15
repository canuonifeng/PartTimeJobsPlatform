package com.parttime.enterprise.pojo.cmd;

import java.time.LocalDate;
import java.time.LocalTime;

public class JobScheduleCmd {

    private LocalDate scheduleDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer slotsAvailable;

    public LocalDate getScheduleDate() { return scheduleDate; }
    public void setScheduleDate(LocalDate scheduleDate) { this.scheduleDate = scheduleDate; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public Integer getSlotsAvailable() { return slotsAvailable; }
    public void setSlotsAvailable(Integer slotsAvailable) { this.slotsAvailable = slotsAvailable; }
}
