package com.broker.service;

import com.broker.dao.ConnectionFactory;
import com.broker.jobs.Broker;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.List;
import java.util.Objects;

@Service
public class BrokerService {

    @Autowired
    private CalculateService calculateService;

    public void register(Broker broker) {
        try {
            PreparedStatement insertBrokerStatement = ConnectionFactory.getInsertBrokerStatement();
            insertBrokerStatement.setString(1, broker.getName());
            insertBrokerStatement.setString(2, broker.getMobile());
            insertBrokerStatement.setString(3, broker.getAccount());
            insertBrokerStatement.setString(4, broker.getPassword());
            insertBrokerStatement.setString(5, broker.getReferrerCode());
            insertBrokerStatement.setLong(6, broker.getParentId());
            insertBrokerStatement.execute();

            if (broker.getParentId() == null) {
                calculateService.brokers.add(broker);
            } else {
                for (Broker b : calculateService.brokers) {
                    if (b.getId().equals(broker.getParentId())) {
                        b.getChildren().add(broker);
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Broker> getBrokers() {

        Broker broker = new Broker();
//        brokers.add(broker);
        broker.setId(1L);
        broker.setSubOrderNums(111);
        broker.setName("sdfjdiofjioe");
        broker.setMobile("11122233311");
        return Lists.newArrayList();
    }

    public String login(String phone, String password) {

        PreparedStatement queryBrokerStatement = ConnectionFactory.getQueryBrokerStatement();
        try {
            queryBrokerStatement.setString(1, phone);
            ResultSet resultSet = queryBrokerStatement.executeQuery();
            if (resultSet.next()) {
                String pwd = resultSet.getString("password");
                if (Objects.equals(password, pwd)) {
                    return resultSet.getString("name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
