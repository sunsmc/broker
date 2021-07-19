package com.broker.vo;

import com.broker.enums.Level;
import lombok.Data;
import lombok.NonNull;

@Data
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
}
