package com.broker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.*;
import java.util.stream.Collectors;

@SpringBootApplication
public class BrokerApplication {

    public static void main(String[] args) {

//        SpringApplication.run(BrokerApplication.class, args);


        int a, c, b;

        b = 2;
        c = 30;

        a = b = c - 1;

        System.out.println(a);
        System.out.println(b);
        System.out.println(c);
    }


}
