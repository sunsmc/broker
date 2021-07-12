package com.broker.bo;

import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;

public class OrderEvent extends ApplicationEvent {

    private Long id;
    private Long brokerId;
    private BigDecimal orderAmountTotal;
    private String type;
    private boolean renewal;

    public OrderEvent(Object source) {
        super(source);
    }

    public boolean isRenewal() {
        return renewal;
    }

    public void setRenewal(boolean renewal) {
        this.renewal = renewal;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBrokerId() {
        return brokerId;
    }

    public void setBrokerId(Long brokerId) {
        this.brokerId = brokerId;
    }

    public BigDecimal getOrderAmountTotal() {
        return orderAmountTotal;
    }

    public void setOrderAmountTotal(BigDecimal orderAmountTotal) {
        this.orderAmountTotal = orderAmountTotal;
    }
}
