package com.broker.bo;

import com.broker.enums.Level;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Accessors(chain = true)
public class Broker {

    /**
     * base elements
     */
    private Long id;
    private Long parentId;
    private String name;
    private String mobile;
    private String account;
    private String accountType;
    private String referrerCode;
    private String password;
    private Date createDate;
    /**
     * advanced elements
     */
    private List<Broker> children = new ArrayList<>();
    //>100
    private Integer orderNums = 0;
    //3>120
    private Integer subOrderNums = 0;

    private Level level = Level.OA;
    private String levelStr = Level.OA.getName();

    private BigDecimal income = BigDecimal.ZERO;
    private BigDecimal directIncome = BigDecimal.ZERO;
    private BigDecimal firstIncome = BigDecimal.ZERO;
    private BigDecimal secondIncome = BigDecimal.ZERO;
    private BigDecimal teamIncome = BigDecimal.ZERO;
    private BigDecimal shopIncome = BigDecimal.ZERO;
    private BigDecimal researchIncome = BigDecimal.ZERO;

}
