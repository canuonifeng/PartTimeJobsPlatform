package com.parttime.platform.api.dto;

import java.util.List;

public class RegistrationListResponse {

    private List<RegistrationResponse> items;
    private int total;

    public List<RegistrationResponse> getItems() { return items; }
    public void setItems(List<RegistrationResponse> items) { this.items = items; }

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
}
