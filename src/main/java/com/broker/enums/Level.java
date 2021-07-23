package com.broker.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public enum Level {

    ordinary(1, "普通",
            BigDecimal.valueOf(0.04), BigDecimal.valueOf(0.01), BigDecimal.valueOf(0.01),
            BigDecimal.valueOf(90), BigDecimal.valueOf(11), BigDecimal.valueOf(6),
            BigDecimal.valueOf(60), BigDecimal.valueOf(8), BigDecimal.valueOf(5)),
    director(2, "主任",
            BigDecimal.valueOf(0.06), BigDecimal.valueOf(0.02), BigDecimal.valueOf(0.02),
            BigDecimal.valueOf(100), BigDecimal.valueOf(13), BigDecimal.valueOf(8),
            BigDecimal.valueOf(70), BigDecimal.valueOf(10), BigDecimal.valueOf(7)),
    manager(3, "经理",
            BigDecimal.valueOf(0.08), BigDecimal.valueOf(0.02), BigDecimal.valueOf(0.02),
            BigDecimal.valueOf(100), BigDecimal.valueOf(15), BigDecimal.valueOf(10),
            BigDecimal.valueOf(80), BigDecimal.valueOf(12), BigDecimal.valueOf(9)),
    chairman(4, "总监",
            BigDecimal.valueOf(0.08), BigDecimal.valueOf(0.04), BigDecimal.valueOf(0.02),
            BigDecimal.valueOf(120), BigDecimal.valueOf(17), BigDecimal.valueOf(12),
            BigDecimal.valueOf(90), BigDecimal.valueOf(14), BigDecimal.valueOf(11)),
    partner(5, "联创",
            BigDecimal.valueOf(0.1), BigDecimal.valueOf(0.04), BigDecimal.valueOf(0.02),
            BigDecimal.valueOf(120), BigDecimal.valueOf(19), BigDecimal.valueOf(14),
            BigDecimal.valueOf(100), BigDecimal.valueOf(16), BigDecimal.valueOf(13));

    private Integer id;
    private String name;

    private BigDecimal direct;
    private BigDecimal first;
    private BigDecimal second;

    private BigDecimal haiDuDirect;
    private BigDecimal haiDuFirst;
    private BigDecimal haiDuSecond;

    private BigDecimal haiDuRenewalDirect;
    private BigDecimal haiDuRenewalFirst;
    private BigDecimal haiDuRenewalSecond;
}
