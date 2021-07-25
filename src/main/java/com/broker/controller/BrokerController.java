package com.broker.controller;

import com.broker.bo.Broker;
import com.broker.bo.HttpResult;
import com.broker.service.BrokerService;
import com.broker.vo.BrokerVO;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/broker")
public class BrokerController {

    @Autowired
    private BrokerService brokerService;

    @RequestMapping("/list")
    public HttpResult<Map<String, Object>> brokerList(@RequestParam("pageIndex") Integer pageIndex,
                                                      @RequestParam("pageSize") Integer pageSize,
                                                      @RequestParam(value = "mobile", required = false) String mobile) {
        Pair<Integer, List<Broker>> pair;
        if (StringUtils.isBlank(mobile)) {
            pair = brokerService.getBrokers(pageSize, (pageIndex - 1) * pageSize);
        } else {
            pair = brokerService.searchBrokers(pageSize, (pageIndex - 1) * pageSize, mobile);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("total", pair.getLeft());
        result.put("list", pair.getRight());
        return HttpResult.success(result);
    }

    @RequestMapping("/export")
    public HttpResult<Void> export(HttpServletResponse httpResponse) {
        return brokerService.export(httpResponse);
    }

    @RequestMapping("/register")
    public HttpResult<Void> register(@RequestBody BrokerVO brokerVO) {
        return brokerService.register(brokerVO);
    }

    @RequestMapping("/login")
    public HttpResult<Broker> login(@RequestParam("phone") String phone, @RequestParam("password") String password) {
        return brokerService.login(phone, password);
    }

    @RequestMapping("/qrcode/{phone}")
    public HttpResult<Void> qrCode(@PathVariable("phone") String phone, HttpServletResponse httpResponse) {

        return brokerService.qrcode(phone, httpResponse);
    }

    @RequestMapping("/{phone}")
    public HttpResult<BrokerVO> getUserInfo(@PathVariable("phone") String phone, HttpServletResponse httpResponse) {

        return brokerService.getUserInfo(phone, httpResponse);
    }
}
