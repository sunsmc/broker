package com.broker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

import java.util.*;
import java.util.stream.Collectors;

@SpringBootApplication
public class BrokerApplication {

    public static void main(String[] args) {

        SpringApplication.run(BrokerApplication.class, args);
    }

}
