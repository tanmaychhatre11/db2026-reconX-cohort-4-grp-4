package com.dbtraining.reconx.dto;

import jakarta.validation.constraints.Pattern;

public class TradeStatusRequest {

    @Pattern(
        regexp = "PENDING|MATCHED|BROKEN|CANCELLED",
        message = "Invalid trade status"
    )
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}