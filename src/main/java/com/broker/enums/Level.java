package com.broker.enums;

import java.math.BigDecimal;

public enum Level {

    ordinary(BigDecimal.valueOf(0.04), BigDecimal.valueOf(0.01), BigDecimal.valueOf(0.01),
            BigDecimal.valueOf(90), BigDecimal.valueOf(11), BigDecimal.valueOf(6),
            BigDecimal.valueOf(60), BigDecimal.valueOf(8), BigDecimal.valueOf(5)),
    director(BigDecimal.valueOf(0.06), BigDecimal.valueOf(0.02), BigDecimal.valueOf(0.02),
            BigDecimal.valueOf(100), BigDecimal.valueOf(13), BigDecimal.valueOf(8),
            BigDecimal.valueOf(70), BigDecimal.valueOf(10), BigDecimal.valueOf(7)),
    manager(BigDecimal.valueOf(0.08), BigDecimal.valueOf(0.02), BigDecimal.valueOf(0.02),
            BigDecimal.valueOf(100), BigDecimal.valueOf(15), BigDecimal.valueOf(10),
            BigDecimal.valueOf(80), BigDecimal.valueOf(12), BigDecimal.valueOf(9)),
    chairman(BigDecimal.valueOf(0.08), BigDecimal.valueOf(0.04), BigDecimal.valueOf(0.02),
            BigDecimal.valueOf(120), BigDecimal.valueOf(17), BigDecimal.valueOf(12),
            BigDecimal.valueOf(90), BigDecimal.valueOf(14), BigDecimal.valueOf(11)),
    partner(BigDecimal.valueOf(0.1), BigDecimal.valueOf(0.04), BigDecimal.valueOf(0.02),
            BigDecimal.valueOf(120), BigDecimal.valueOf(19), BigDecimal.valueOf(14),
            BigDecimal.valueOf(100), BigDecimal.valueOf(16), BigDecimal.valueOf(13));

    private BigDecimal direct;
    private BigDecimal first;
    private BigDecimal second;

    private BigDecimal haiDuDirect;
    private BigDecimal haiDuFirst;
    private BigDecimal haiDuSecond;

    private BigDecimal haiDuRenewalDirect;
    private BigDecimal haiDuRenewalFirst;
    private BigDecimal haiDuRenewalSecond;

    Level(BigDecimal direct, BigDecimal first, BigDecimal second, BigDecimal haiDuDirect, BigDecimal haiDuFirst, BigDecimal haiDuSecond, BigDecimal haiDuRenewalDirect, BigDecimal haiDuRenewalFirst, BigDecimal haiDuRenewalSecond) {
        this.direct = direct;
        this.first = first;
        this.second = second;
        this.haiDuDirect = haiDuDirect;
        this.haiDuFirst = haiDuFirst;
        this.haiDuSecond = haiDuSecond;
        this.haiDuRenewalDirect = haiDuRenewalDirect;
        this.haiDuRenewalFirst = haiDuRenewalFirst;
        this.haiDuRenewalSecond = haiDuRenewalSecond;
    }

    public BigDecimal getDirect() {
        return direct;
    }

    public BigDecimal getFirst() {
        return first;
    }

    public BigDecimal getSecond() {
        return second;
    }

    public BigDecimal getHaiDuDirect() {
        return haiDuDirect;
    }

    public BigDecimal getHaiDuFirst() {
        return haiDuFirst;
    }

    public BigDecimal getHaiDuSecond() {
        return haiDuSecond;
    }

    public BigDecimal getHaiDuRenewalDirect() {
        return haiDuRenewalDirect;
    }

    public BigDecimal getHaiDuRenewalFirst() {
        return haiDuRenewalFirst;
    }

    public BigDecimal getHaiDuRenewalSecond() {
        return haiDuRenewalSecond;
    }
}
