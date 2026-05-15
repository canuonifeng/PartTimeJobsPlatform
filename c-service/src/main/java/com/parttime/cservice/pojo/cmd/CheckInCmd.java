package com.parttime.cservice.pojo.cmd;

import java.math.BigDecimal;

public class CheckInCmd {

    private Long shiftId;
    private BigDecimal lat;
    private BigDecimal lng;

    public Long getShiftId() { return shiftId; }
    public void setShiftId(Long shiftId) { this.shiftId = shiftId; }

    public BigDecimal getLat() { return lat; }
    public void setLat(BigDecimal lat) { this.lat = lat; }

    public BigDecimal getLng() { return lng; }
    public void setLng(BigDecimal lng) { this.lng = lng; }
}
