package com.broker.vo;

import com.broker.enums.Level;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BrokerVO {

    @NonNull
    private String name;
    @NonNull
    private String mobile;
    @NonNull
    private String account;
    @NonNull
    private String referrerCode;
    @NonNull
    private String password;

    private String accountType;

    List<BrokerVO> children;

    private String levelStr;
}
