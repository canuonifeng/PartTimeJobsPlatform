package com.parttime.cservice.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

public class JobScheduleInfoVO {
    private Long id;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    private String startTime;
    private String endTime;
    private Integer slotsAvailable;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public Integer getSlotsAvailable() { return slotsAvailable; }
    public void setSlotsAvailable(Integer slotsAvailable) { this.slotsAvailable = slotsAvailable; }
}
