package com.broker.controller;

import com.broker.jobs.Broker;
import com.broker.service.BrokerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@RestController
@CrossOrigin
@RequestMapping("/broker")
public class BrokerController {

    @Autowired
    private BrokerService brokerService;

    @RequestMapping("/list")
    public Map<String, Object> brokerList(@RequestParam("pageIndex") Integer pageIndex,
                                          @RequestParam("pageSize") Integer pageSize) {

        List<Broker> brokers = brokerService.getBrokers();
        Map<String, Object> result = new HashMap<>();
        result.put("total", brokers.size());
        result.put("list", brokers.stream().skip((pageIndex - 1) * pageSize).limit(pageSize).collect(toList()));
        return result;
    }

    @RequestMapping("/register")
    public void register(@RequestBody Broker broker) {

        brokerService.register(broker);
    }

    @RequestMapping("/login")
    public String login(@RequestParam("phone") String phone, @RequestParam("password") String password) {

        return brokerService.login(phone, password);
    }

    @RequestMapping("/{phone}")
    public String login(@PathVariable("phone") String phone) {

        return "fkdopdkfdpfkdp";
    }
}
