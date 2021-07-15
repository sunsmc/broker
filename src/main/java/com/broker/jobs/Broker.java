package com.broker.jobs;

import com.broker.enums.Level;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Broker {

    /**
     * base elements
     */
    private Long id;
    private String name;
    private String mobile;
    private String account;
    private Long parentId;
    private Level level = Level.ordinary;
    private String referrerCode;
    private String password;
    /**
     * advanced elements
     */
    private List<Broker> children = new ArrayList<>();
    //>100
    private Integer orderNums = 0;
    //3>120
    private Integer subOrderNums = 0;

    private BigDecimal income = BigDecimal.ZERO;

    public String getReferrerCode() {
        return referrerCode;
    }

    public void setReferrerCode(String referrerCode) {
        this.referrerCode = referrerCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public Integer getSubOrderNums() {
        return subOrderNums;
    }

    public void setSubOrderNums(Integer subOrderNums) {
        this.subOrderNums = subOrderNums;
    }

    public BigDecimal getIncome() {
        return income;
    }

    public void setIncome(BigDecimal income) {
        this.income = income;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public List<Broker> getChildren() {
        return children;
    }

    public void setChildren(List<Broker> children) {
        this.children = children;
    }

    public Integer getOrderNums() {
        return orderNums;
    }

    public void setOrderNums(Integer orderNums) {
        this.orderNums = orderNums;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
